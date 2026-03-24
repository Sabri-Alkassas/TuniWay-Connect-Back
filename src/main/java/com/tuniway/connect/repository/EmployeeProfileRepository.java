package com.tuniway.connect.repository;

import com.tuniway.connect.model.entity.EmployeeProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfile, UUID> {
    Optional<EmployeeProfile> findByEmployeeCode(String employee_code);
    Optional<EmployeeProfile> findByLicenseNumber(String licenseNumber);
    Optional<EmployeeProfile> findByUserId(UUID userId);
    boolean existsByEmployeeCode(String employee_code);
    boolean existsByLicenseNumber(String LicenseNumber);
}