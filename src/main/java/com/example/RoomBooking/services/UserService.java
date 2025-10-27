package com.example.RoomBooking.services;

import com.example.RoomBooking.dto.ChangePasswordRequest;
import com.example.RoomBooking.dto.RegisterRequest;
import com.example.RoomBooking.dto.UserRequest;
import com.example.RoomBooking.dto.UserResponse;
import com.example.RoomBooking.exceptions.ForbiddenException;
import com.example.RoomBooking.exceptions.ResourceAlreadyExistsException;
import com.example.RoomBooking.exceptions.ResourceNotFoundException;
import com.example.RoomBooking.models.Property;
import com.example.RoomBooking.models.Role;
import com.example.RoomBooking.models.User;
import com.example.RoomBooking.models.UserStatus;
import com.example.RoomBooking.repositories.PropertyRepository;
import com.example.RoomBooking.repositories.RoleRepository;
import com.example.RoomBooking.repositories.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PropertyRepository propertyRepository;
    private final PropertyService propertyService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String USER_STATUS_KEY = "user_status";
    private static final long OFFLINE_THRESHOLD = 120000;
    @Value("${spring.mail.username}")
    private String fromEmail;

    public UserResponse registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new ResourceAlreadyExistsException("Username already exists!");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ResourceAlreadyExistsException("Email already exists!");
        }
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setFullname(registerRequest.getFullname());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setGender(registerRequest.getGender());
        user.setBirthdate(registerRequest.getBirthdate());

        if (isInvalidPassword(registerRequest.getPassword())) {
            throw new RuntimeException("New password does not meet complexity requirements");
        }
        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
        user.setPassword(encodedPassword);

        Role defaultRole = roleRepository.findByRoleName("User")
                .orElseThrow(() -> new RuntimeException("Default role not found"));
        Set<Role> roles = new HashSet<>();
        roles.add(defaultRole);
        user.setRoles(roles);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return mapToResponse(user, true);
    }

    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return mapToResponse(user, false);
    }

    public UserResponse getUserByUsername(String userName) {
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + userName));
        return mapToResponse(user, true);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> mapToResponse(user, false))
                .collect(Collectors.toList());
    }

    public void updateUser(Long userId, UserRequest userRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setFullname(userRequest.getFullname());
        user.setPhoneNumber(userRequest.getPhoneNumber());
        user.setGender(userRequest.getGender());
        user.setBirthdate(userRequest.getBirthdate());
        user.setAvatar(userRequest.getAvatar());
        user.setPublicProfile(userRequest.isPublicProfile());
        user.setPublicPhone(userRequest.isPublicPhone());
        user.setPublicEmail(userRequest.isPublicEmail());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public void deleteUserWithPermission(String currentUsername, Long targetUserId) {
        UserResponse currentUser = getUserByUsername(currentUsername);
        UserResponse targetUser = getUserById(targetUserId);

        boolean isSelfDelete = currentUser.getId().equals(targetUserId);
        boolean isAdmin = currentUser.getRoles().contains("Admin");
        boolean isOwner = currentUser.getRoles().contains("Owner");
        boolean targetIsOwner = targetUser.getRoles().contains("Owner");
        boolean targetIsAdmin = targetUser.getRoles().contains("Admin");

        if (!isAdmin && !isOwner) {
            if (!isSelfDelete) {
                throw new ForbiddenException("User chỉ được tự xoá tài khoản của mình");
            }
        } else if (isAdmin && !isOwner) {
            if (targetIsOwner || targetIsAdmin) {
                throw new ForbiddenException("Admin không được xoá Owner hoặc Admin khác");
            }
        } else {
            if (targetIsOwner && !isSelfDelete) {
                throw new ForbiddenException("Owner không thể xoá Owner khác");
            }
        }

        deleteUser(targetUserId);
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        redisTemplate.opsForHash().delete(USER_STATUS_KEY, userId.toString());
        userRepository.delete(user);
    }

    public void changePassword(Long userId, ChangePasswordRequest changePasswordRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!passwordEncoder.matches(changePasswordRequest.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (isInvalidPassword(changePasswordRequest.getNewPassword())) {
            throw new RuntimeException("New password does not meet complexity requirements");
        }

        if (passwordEncoder.matches(changePasswordRequest.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("New password must be different from the current password");
        }

        String encodedPassword = passwordEncoder.encode(changePasswordRequest.getNewPassword());
        user.setPassword(encodedPassword);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private boolean isInvalidPassword(String password) {
        String passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$";
        return !password.matches(passwordRegex);
    }

    public void initiatePasswordReset(String email) throws MessagingException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setTokenCreationDate(LocalDateTime.now());
        userRepository.save(user);

        emailService.sendResetPasswordEmail(user.getEmail(), user.getFullname(), token);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid token"));

        if (user.getTokenCreationDate().plusHours(24).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setTokenCreationDate(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Transactional
    public void addPropertyToFavorites(Long userId, Long propertyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + propertyId));
        if (user.getFavoriteProperties().contains(property)) {
            throw new IllegalArgumentException("Property is already in your favorites");
        }
        propertyService.incrementPropertyLike(propertyId);
        user.getFavoriteProperties().add(property);
        userRepository.save(user);
    }

    private String getFirstName(String fullname) {
        if (fullname != null && !fullname.isEmpty()) {
            String[] parts = fullname.split(" ");
            return parts[parts.length - 1];
        }
        return fullname;
    }

    public String checkUserLockStatus(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username:" + username));
        if (user != null && user.isLocked()) {
            String lockReason = user.getLockReason();
            LocalDateTime expirationDate = user.getLockExpirationDate();
            return "Tài khoản của bạn đã bị khóa. Lý do: " + lockReason + ", Hết hạn vào: " + expirationDate;
        }
        return null;
    }

    private UserResponse mapToResponse(User user, boolean isOwner) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        if (isOwner || user.isPublicEmail()) {
            response.setEmail(user.getEmail());
        } else {
            String email = user.getEmail();
            String maskedEmail = email.replaceAll("(?<=.).(?=[^@]*?.@)", "*");
            response.setEmail(maskedEmail);
        }
        if (isOwner || user.isPublicPhone()) {
            response.setPhoneNumber(user.getPhoneNumber());
        } else {
            String phoneNumber = user.getPhoneNumber();
            String maskedPhoneNumber = phoneNumber.replaceAll(".(?=.{4})", "*");
            response.setPhoneNumber(maskedPhoneNumber);
        }
        if (isOwner || user.isPublicProfile()) {
            response.setFullname(user.getFullname());
            response.setBirthdate(user.getBirthdate());
        } else {
            String fullname = user.getFullname();
            String firstName = getFirstName(fullname);

            String gender = user.getGender();
            String formattedName;

            if ("Nam".equalsIgnoreCase(gender)) {
                formattedName = "Mr. " + firstName;
            } else if ("Nữ".equalsIgnoreCase(gender)) {
                formattedName = "Mrs. " + firstName;
            } else {
                formattedName = firstName;
            }
            response.setFullname(formattedName);
            response.setBirthdate(null);
        }
        response.setGender(user.getGender());
        response.setAvatar(user.getAvatar());
        response.setRoles(user.getRoles().stream().map(Role::getRoleName).collect(Collectors.toSet()));
        response.setFavoritePropertyIds(user.getFavoriteProperties().stream().map(Property::getId).collect(Collectors.toSet()));
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        response.setPublicEmail(user.isPublicEmail());
        response.setPublicPhone(user.isPublicPhone());
        response.setPublicProfile(user.isPublicProfile());
        return response;
    }

    public void updateUserStatus(Long userId, String status) {
        redisTemplate.opsForHash().put(USER_STATUS_KEY, userId.toString(), status + ":" + Instant.now().toEpochMilli());
    }

    public UserStatus getUserStatus(Long userId) {
        String statusData = (String) redisTemplate.opsForHash().get(USER_STATUS_KEY, userId.toString());

        if (statusData == null) {
            return new UserStatus("offline", null);
        }

        String[] parts = statusData.split(":");
        if (parts.length != 2) {
            return new UserStatus("offline", null);
        }

        String status = parts[0];
        Long timestamp = Long.parseLong(parts[1]);

        return new UserStatus(status, timestamp);
    }

    @Scheduled(fixedRate = 60000)
    public void cleanupOldStatus() {
        Set<Object> keys = redisTemplate.opsForHash().keys(USER_STATUS_KEY);
        long now = Instant.now().toEpochMilli();

        for (Object key : keys) {
            String userIdStr = (String) key;
            String statusData = (String) redisTemplate.opsForHash().get(USER_STATUS_KEY, userIdStr);
            if (statusData != null) {
                String[] parts = statusData.split(":");
                if (parts.length == 2) {
                    try {
                        long lastHeartbeat = Long.parseLong(parts[1]);
                        if (now - lastHeartbeat > OFFLINE_THRESHOLD) {
                            redisTemplate.opsForHash().delete(USER_STATUS_KEY, userIdStr);
                        }
                    } catch (NumberFormatException e) {
                        redisTemplate.opsForHash().delete(USER_STATUS_KEY, userIdStr);
                    }
                } else {
                    redisTemplate.opsForHash().delete(USER_STATUS_KEY, userIdStr);
                }
            }
        }
    }
}
