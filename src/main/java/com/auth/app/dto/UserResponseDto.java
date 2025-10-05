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

    private UUID userId;

    private String fullName;

    private String email;

    private String phoneNumber;

    private Role role;

    private UserType userType;

    private UUID companyId;

    private String companyName;

    private UUID customerId;

    private String customerName;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
