package com.auth.app.dto;

import com.auth.app.entity.Role;
import com.auth.app.entity.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {

    private Long userId;

    private String fullName;

    private String email;

    private String phoneNumber;

    private Role role;

    private UserType userType;

    private Long companyId;

    private String companyName;

    private Long customerId;

    private String customerName;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
