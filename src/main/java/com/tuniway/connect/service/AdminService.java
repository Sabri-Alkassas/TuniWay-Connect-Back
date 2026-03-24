package com.tuniway.connect.service;

import com.tuniway.connect.model.dto.RegisterEmployeeRequest;
import com.tuniway.connect.model.dto.RegisterEmployeeResponse;
import com.tuniway.connect.model.dto.UpdatedEmployeeRequest;
import com.tuniway.connect.model.dto.UpdatedEmployeeResponse;
import com.tuniway.connect.model.entity.AccountStatus;
import com.tuniway.connect.model.entity.EmployeeProfile;
import com.tuniway.connect.model.entity.Role;
import com.tuniway.connect.model.entity.User;
import com.tuniway.connect.repository.EmployeeProfileRepository;
import com.tuniway.connect.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final EmployeeProfileRepository employeeProfileRepository;

    public AdminService(UserRepository userRepository, EmployeeProfileRepository employeeProfileRepository) {
        this.userRepository = userRepository;
        this.employeeProfileRepository = employeeProfileRepository;
    }

    public UpdatedEmployeeResponse updateEmployee(UUID employeeId, UpdatedEmployeeRequest request) {
        User user = userRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        EmployeeProfile profile = employeeProfileRepository.findByUserId(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee profile not found"));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            profile.setFullName(request.getFullName());
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            profile.setPhone(request.getPhone());
        }

        if (request.getLicense_number() != null && !request.getLicense_number().isBlank()) {
            if (employeeProfileRepository.existsByLicenseNumber(request.getLicense_number())) {
                throw new IllegalArgumentException("An employee with this license number already exists");
            }
            profile.setLicenseNumber(request.getLicense_number());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("An employee with this email already exists");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPassword_hash() != null && !request.getPassword_hash().isBlank()) {
            user.setPassword_hash(request.getPassword_hash());
        }

        if (request.getEmployee_code() != null && !request.getEmployee_code().isBlank()) {
            if (employeeProfileRepository.existsByEmployeeCode(request.getEmployee_code())) {
                throw new IllegalArgumentException("An employee with this employee code already exists");
            }
            profile.setEmployeeCode(request.getEmployee_code());
        }

        employeeProfileRepository.save(profile);
        userRepository.save(user);

        UpdatedEmployeeResponse response = new UpdatedEmployeeResponse();
        response.setSuccess(true);
        response.setMessage("Employee updated successfully");
        return response;
    }

    public RegisterEmployeeResponse registerEmployee(RegisterEmployeeRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank() ||
                request.getPassword_hash() == null || request.getPassword_hash().isBlank() ||
                request.getFullName() == null || request.getFullName().isBlank()) {
            throw new IllegalArgumentException("Email, password, and full name are required");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("An employee with this email already exists");
        }

        if (employeeProfileRepository.existsByEmployeeCode(request.getEmployee_code())) {
            throw new IllegalArgumentException("An employee with this employee code already exists");
        }

        if (employeeProfileRepository.existsByLicenseNumber(request.getLicense_number())) {
            throw new IllegalArgumentException("An employee with this license number already exists");
        }

        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setEmail(request.getEmail());
        user.setPassword_hash(request.getPassword_hash());
        user.setRole(Role.EMPLOYEE);
        user.setStatus(AccountStatus.ACTIVE);

        User savedUser = userRepository.save(user);

        EmployeeProfile profile = new EmployeeProfile();
        profile.setUserId(savedUser.getId());
        profile.setFullName(request.getFullName());
        profile.setPhone(request.getPhone());
        profile.setLicenseNumber(request.getLicense_number());
        profile.setEmployeeCode(request.getEmployee_code());

        EmployeeProfile savedProfile = employeeProfileRepository.save(profile);

        RegisterEmployeeResponse response = new RegisterEmployeeResponse();
        response.setId(savedUser.getId());
        response.setEmail(savedUser.getEmail());
        response.setRole(savedUser.getRole());
        response.setStatus(savedUser.getStatus());
        response.setCreatedAt(savedUser.getCreatedAt());
        response.setFullName(savedProfile.getFullName());
        response.setPhone(savedProfile.getPhone());
        response.setLicense_number(savedProfile.getLicenseNumber());
        response.setEmployee_code(savedProfile.getEmployeeCode());
        response.setSuccess(true);
        response.setMessage("Employee registered successfully");

        return response;
    }
}
