package com.auth.app.service;



import com.auth.app.dto.CompanyDto;
import com.auth.app.entity.Company;
import com.auth.app.entity.User;
import com.auth.app.repository.CompanyRepository;
import com.auth.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    @Transactional
    public Company saveCompany(CompanyDto companyDto, Long userId) {
        // 1. Company oluştur
        Company company = new Company();
        company.setCompanyName(companyDto.getCompanyName());
        company.setEmail(companyDto.getEmail());
        company.setPhoneNumber(companyDto.getPhoneNumber());
        company.setAddress(companyDto.getAddress());
        company.setCity(companyDto.getCity());
        company.setDistrict(companyDto.getDistrict());
        company.setNeighborhood(companyDto.getNeighborhood());
        company.setSector(companyDto.getSector());
        company.setDescription(companyDto.getDescription());
        company.setContactInfo(companyDto.getContactInfo());

        // 2. Company kaydet
        Company savedCompany = companyRepository.save(company);

        // 3. User'a referans setle
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setCompany(savedCompany);
        userRepository.save(user); // Güncelleme

        return savedCompany;
    }
}
