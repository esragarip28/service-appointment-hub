package com.auth.app.dto;


import com.auth.app.entity.Role;
import com.auth.app.entity.UserType;
import lombok.Data;

@Data
public class UserRequestDto {
    private String fullName;
    private String email;
    private String phoneNumber;
    private String password;  // raw password
    private Role role;
    private UserType userType;
}
