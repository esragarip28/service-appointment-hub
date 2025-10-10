package com.auth.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDto {

    private String companyName;
    private String email;
    private String phoneNumber;
    private String contactInfo;
    private String address;
    private String city;
    private String district;
    private String neighborhood;
    private String sector;
    private String description;

}
