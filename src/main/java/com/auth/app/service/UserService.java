package com.auth.app.service;

import com.auth.app.dto.UserRequestDto;
import com.auth.app.dto.UserResponseDto;
import com.auth.app.entity.Company;
import com.auth.app.entity.Customer;
import com.auth.app.entity.User;
import com.auth.app.exception.UserNotFoundException;
import com.auth.app.repository.CompanyRepository;
import com.auth.app.repository.CustomerRepository;
import com.auth.app.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, CompanyRepository companyRepository, CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserNotFoundException("User not authenticated");
        }

        String email = authentication.getName();

        User user = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        return mapToUserResponseDto(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        return mapToUserResponseDto(user);
    }

    private UserResponseDto mapToUserResponseDto(User user) {
        return UserResponseDto.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .userType(user.getUserType())
                .companyId(user.getCompany() != null ? user.getCompany().getCompanyId() : null)
                .companyName(user.getCompany() != null ? user.getCompany().getCompanyName() : null)
                .customerId(user.getCustomer() != null ? user.getCustomer().getCustomerId() : null)
                .customerName(user.getCustomer() != null ? user.getCustomer().getCustomerName() : null)
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }


    public User createUser(UserRequestDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());
        user.setUserType(dto.getUserType());

        return userRepository.save(user);
    }

    public List<UserResponseDto> findAll(){
        List<UserResponseDto> userResponseDtos = new ArrayList<>();
       List<User> users = userRepository.findAll();
       if (!CollectionUtils.isEmpty(users)){
           users.forEach(u->{
               UserResponseDto userResponseDto = new UserResponseDto();
               userResponseDto.setUserId(u.getUserId());
               userResponseDto.setActive(u.getActive());
               userResponseDto.setUserType(u.getUserType());
               userResponseDto.setEmail(u.getEmail());
               userResponseDto.setCreatedAt(u.getCreatedAt());
               userResponseDto.setFullName(u.getFullName());
               userResponseDto.setPhoneNumber(u.getPhoneNumber());

               userResponseDtos.add(userResponseDto);
           });
       }
       return userResponseDtos;
    }

    public Page<UserResponseDto> findAllByCriteria(String fullName, String email, String role, Pageable pageable) {
        Page<User> users = userRepository.findAllWithFilters(fullName, email, role, pageable);
        Page<UserResponseDto> dtoPage = users.map(u -> {
            UserResponseDto userResponseDto = new UserResponseDto();
            userResponseDto.setUserId(u.getUserId());
            userResponseDto.setActive(u.getActive());
            userResponseDto.setUserType(u.getUserType());
            userResponseDto.setEmail(u.getEmail());
            userResponseDto.setCreatedAt(u.getCreatedAt());
            userResponseDto.setFullName(u.getFullName());
            userResponseDto.setPhoneNumber(u.getPhoneNumber());
            return userResponseDto;
        });

        return dtoPage;
    }

}
