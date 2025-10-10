package com.auth.app.repository;

import com.auth.app.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByEmail(String email);

    boolean existsByEmail(String email);
}
