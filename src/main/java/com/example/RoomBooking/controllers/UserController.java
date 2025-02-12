package com.example.RoomBooking.controllers;

import com.example.RoomBooking.dto.*;
import com.example.RoomBooking.exceptions.ResourceNotFoundException;
import com.example.RoomBooking.models.UserStatus;
import com.example.RoomBooking.models.UserStatusUpdate;
import com.example.RoomBooking.services.JwtTokenProvider;
import com.example.RoomBooking.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/id/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/username/{userName}")
    public  ResponseEntity<UserResponse> getUserByUsername(@PathVariable String userName) {
        UserResponse user = userService.getUserByUsername(userName);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/info")
    public ResponseEntity<UserResponse> getUserInfo(@RequestHeader("Authorization") String token) {
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

    @PutMapping("/{userId}/password")
    public ResponseEntity<?> changePassword(@PathVariable Long userId, @RequestBody ChangePasswordRequest changePasswordRequest) {
        userService.changePassword(userId, changePasswordRequest);
        return ResponseEntity.ok("Password changed successfully.");
    }

    @PostMapping("/{userId}/favorites/{propertyId}")
    public ResponseEntity<String> addPropertyToFavorites(@PathVariable Long userId, @PathVariable Long propertyId) {
        userService.addPropertyToFavorites(userId, propertyId);
        return ResponseEntity.ok("Property added to favorites successfully");
    }

    @MessageMapping("/user-status")
    public void handleUserStatus(UserStatusDTO userStatusDTO) {
        userService.updateUserStatus(userStatusDTO.getUserId(), userStatusDTO.getStatus());
        broadcastUserStatus(userStatusDTO.getUserId(), userStatusDTO.getStatus());
    }

    @GetMapping("/status/{userId}")
    public ResponseEntity<UserStatus> getUserStatus(@PathVariable Long userId) {
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

