package com.auth.app.controller;

import com.auth.app.dto.UserRequestDto;
import com.auth.app.dto.UserResponseDto;
import com.auth.app.entity.User;
import com.auth.app.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser() {
        UserResponseDto response = userService.getCurrentUser();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable UUID userId) {
        UserResponseDto response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create/user")
    public ResponseEntity<User> createUser(@RequestBody UserRequestDto dto) {
        User user = userService.createUser(dto);
        return ResponseEntity.ok(user);
    }
    @GetMapping("/get/all")
    public ResponseEntity<List<UserResponseDto>> getAllUser() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/get/all/by/criteria")
    public ResponseEntity<List<UserResponseDto>> getAllUser(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            Pageable pageable) {
         List<UserResponseDto> users = userService.findAllByCriteria(fullName, email, role, pageable);
        return ResponseEntity.ok(users);
    }


}
