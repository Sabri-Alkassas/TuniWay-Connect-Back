package com.tuniway.connect.repository;

import com.tuniway.connect.model.entity.EmployeeProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfile, UUID> {
    Optional<EmployeeProfile> findByEmployee_code(String employee_code);
    Optional<EmployeeProfile> findByLicense_number(String license_number);
    boolean existsByEmployee_code(String employee_code);
    boolean existsByLicense_number(String license_number);
}