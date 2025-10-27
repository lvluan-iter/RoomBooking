package com.example.RoomBooking.controllers;

import com.example.RoomBooking.dto.*;
import com.example.RoomBooking.models.UserStatus;
import com.example.RoomBooking.models.UserStatusUpdate;
import com.example.RoomBooking.services.JwtTokenProvider;
import com.example.RoomBooking.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final JwtTokenProvider tokenProvider;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/id/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/username/{userName}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String userName) {
        UserResponse user = userService.getUserByUsername(userName);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String token) {
        String jwtToken = token.substring(7);
        String username = tokenProvider.getUsernameFromToken(jwtToken);
        UserResponse user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody UserRequest userRequest) {
        userService.updateUser(userId, userRequest);
        return ResponseEntity.ok("User updated successfully.");
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long userId,
            Authentication authentication) {

        userService.deleteUserWithPermission(authentication.getName(), userId);
        return ResponseEntity.ok(ApiResult.success("Deleted successfully"));
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<?> changePassword(@PathVariable Long userId, @RequestBody ChangePasswordRequest changePasswordRequest) {
        userService.changePassword(userId, changePasswordRequest);
        return ResponseEntity.ok("Password changed successfully.");
    }

    @PostMapping("/{userId}/favorites/{propertyId}")
    public ResponseEntity<?> addPropertyToFavorites(@PathVariable Long userId, @PathVariable Long propertyId) {
        userService.addPropertyToFavorites(userId, propertyId);
        return ResponseEntity.ok("Property added to favorites successfully");
    }

    @MessageMapping("/user-status")
    public void handleUserStatus(UserStatusDTO userStatusDTO) {
        userService.updateUserStatus(userStatusDTO.getUserId(), userStatusDTO.getStatus());
        broadcastUserStatus(userStatusDTO.getUserId(), userStatusDTO.getStatus());
    }

    @GetMapping("/status/{userId}")
    public ResponseEntity<?> getUserStatus(@PathVariable Long userId) {
        UserStatus status = userService.getUserStatus(userId);

        if (status == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(status);
    }

    @MessageMapping("/user-status/get")
    public void handleGetUserStatus(@Payload Long requestedUserId) {
        UserStatus status = userService.getUserStatus(requestedUserId);
        messagingTemplate.convertAndSend("/topic/user-status", new UserStatusUpdate(requestedUserId, status));
    }

    private void broadcastUserStatus(Long userId, String status) {
        UserStatus userStatus = new UserStatus(status, System.currentTimeMillis());
        messagingTemplate.convertAndSend("/topic/user-status", new UserStatusUpdate(userId, userStatus));
    }
}

