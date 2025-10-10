package com.auth.app.dto;

import com.auth.app.entity.Role;
import com.auth.app.entity.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDto {

    private String accessToken;

    private String refreshToken;

    private String tokenType = "Bearer";

    private Long expiresIn;

    private Long userId;

    private String fullName;

    private String email;

    private Role role;

    private UserType userType;

    private Long companyId;

    private Long customerId;
}
