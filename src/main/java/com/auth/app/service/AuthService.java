package com.auth.app.service;

import com.auth.app.dto.*;
import com.auth.app.entity.*;
import com.auth.app.exception.InvalidCredentialsException;
import com.auth.app.exception.TokenExpiredException;
import com.auth.app.exception.UserAlreadyExistsException;
import com.auth.app.repository.CompanyRepository;
import com.auth.app.repository.CustomerRepository;
import com.auth.app.repository.UserRepository;
import com.auth.app.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       CompanyRepository companyRepository,
                       CustomerRepository customerRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public MessageResponseDto register(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Registration attempt with existing email: {}", request.getEmail());
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setUserType(request.getUserType());
        user.setActive(true);

        switch (request.getUserType()) {
            case COMPANY:
                user.setRole(Role.COMPANY);

                Company company = new Company();
                company.setCompanyName(request.getCompanyName() != null ? request.getCompanyName() : request.getFullName());
                company.setEmail(request.getCompanyEmail() != null ? request.getCompanyEmail() : request.getEmail());
                company.setPhoneNumber(request.getCompanyPhone() != null ? request.getCompanyPhone() : request.getPhoneNumber());
                company.setAddress(request.getCompanyAddress());

                company = companyRepository.save(company);
                user.setCompany(company);
                break;

            case CUSTOMER:
                user.setRole(Role.USER);

                Customer customer = new Customer();
                customer.setCustomerName(request.getCustomerName() != null ? request.getCustomerName() : request.getFullName());
                customer.setEmail(request.getCustomerEmail() != null ? request.getCustomerEmail() : request.getEmail());
                customer.setPhoneNumber(request.getCustomerPhone() != null ? request.getCustomerPhone() : request.getPhoneNumber());
                customer.setAddress(request.getCustomerAddress());

                customer = customerRepository.save(customer);
                user.setCustomer(customer);
                break;

            case ADMIN:
                user.setRole(Role.ADMIN);
                break;
        }

        userRepository.save(user);

        logger.info("User registered successfully: {}", request.getEmail());

        return new MessageResponseDto("User registered successfully");
    }

    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByEmailAndActiveTrue(request.getEmail())
                .orElseThrow(() -> {
                    logger.warn("Login attempt with invalid email: {}", request.getEmail());
                    return new InvalidCredentialsException("Invalid email or password");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            logger.warn("Login attempt with invalid password for email: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        logger.info("User logged in successfully: {}", request.getEmail());

        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getAccessTokenExpiration())
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .userType(user.getUserType())
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .customerId(user.getCustomer() != null ? user.getCustomer().getId() : null)
                .build();
    }




    public MessageResponseDto logout() {
        logger.info("User logout request received");
        return new MessageResponseDto("Logout successful");
    }

    @Transactional(readOnly = true)
    public AuthResponseDto refreshToken(RefreshTokenRequestDto request) {
        try {
            String email = jwtUtil.extractEmail(request.getRefreshToken());

            if (jwtUtil.isTokenExpired(request.getRefreshToken())) {
                throw new TokenExpiredException("Refresh token has expired");
            }

            User user = userRepository.findByEmailAndActiveTrue(email)
                    .orElseThrow(() -> new InvalidCredentialsException("User not found or inactive"));

            String newAccessToken = jwtUtil.generateAccessToken(user);
            String newRefreshToken = jwtUtil.generateRefreshToken(user);

            logger.info("Token refreshed successfully for user: {}", email);

            return AuthResponseDto.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getAccessTokenExpiration())
                    .userId(user.getId())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .userType(user.getUserType())
                    .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                    .customerId(user.getCustomer() != null ? user.getCustomer().getId() : null)
                    .build();

        } catch (Exception e) {
            logger.error("Error refreshing token: {}", e.getMessage());
            throw new TokenExpiredException("Invalid or expired refresh token");
        }
    }
}
