package com.auth.app.controller;

import com.auth.app.dto.CompanyDto;
import com.auth.app.entity.Company;
import com.auth.app.service.CompanyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping("/save")
    public ResponseEntity<Company> saveCompany(
            @RequestBody CompanyDto companyDto,
            @RequestParam Long userId
    ) {
        Company savedCompany = companyService.saveCompany(companyDto, userId);
        return ResponseEntity.ok(savedCompany);
    }
}
