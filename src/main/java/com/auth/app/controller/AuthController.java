    package com.auth.app.controller;

import com.auth.app.dto.*;
import com.auth.app.entity.Role;
import com.auth.app.entity.User;
import com.auth.app.repository.UserRepository;
import com.auth.app.service.AuthService;
import com.auth.app.service.GoogleAuthService;
import com.auth.app.util.JwtUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

    @RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    private final GoogleAuthService googleAuthService;

    private final UserRepository userRepository;

    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, GoogleAuthService googleAuthService, UserRepository userRepository, JwtUtil jwtUtil) {
        this.authService = authService;
        this.googleAuthService = googleAuthService;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        MessageResponseDto response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "admin1234";  // mevcut düz şifre
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("Encoded password: "+encodedPassword);
        AuthResponseDto response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @PostMapping("/login/google")
    public ResponseEntity<AuthResponseDto> googleLogin(@RequestBody GoogleLoginRequest request) {
        // 1. Token doğrula
        GoogleIdToken.Payload payload = null;
        try {
            payload = googleAuthService.verifyToken(request.getToken());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google token", e);
        }

        String email = payload.getEmail();
        String fullName = (String) payload.get("name");

        // 2. DB’de kullanıcıyı bul
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    // Yeni kullanıcı yarat
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setFullName(fullName);
                    newUser.setRole(Role.USER);
                    return userRepository.save(newUser);
                });

        // 3. JWT üret
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        return ResponseEntity.ok(
                AuthResponseDto.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .tokenType("Bearer")
                        .expiresIn(jwtUtil.getAccessTokenExpiration())
                        .userId(user.getUserId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .userType(user.getUserType())
                        .build()
        );
    }


    @PostMapping("/logout")
    public ResponseEntity<MessageResponseDto> logout() {
        MessageResponseDto response = authService.logout();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto request) {
        AuthResponseDto response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }
}
