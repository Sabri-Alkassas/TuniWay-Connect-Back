package com.tuniway.connect.service;

import com.tuniway.connect.model.dto.CreateTransportRequest;
import com.tuniway.connect.model.dto.RegisterEmployeeRequest;
import com.tuniway.connect.model.dto.RegisterEmployeeResponse;
import com.tuniway.connect.model.dto.TransportResponse;
import com.tuniway.connect.model.dto.UpdatedEmployeeRequest;
import com.tuniway.connect.model.dto.UpdatedEmployeeResponse;
import com.tuniway.connect.model.dto.UpdatedEmployeeStatusRequest;
import com.tuniway.connect.model.entity.AccountStatus;
import com.tuniway.connect.model.entity.EmployeeProfile;
import com.tuniway.connect.model.entity.Role;
import com.tuniway.connect.model.entity.Transport;
import com.tuniway.connect.model.entity.User;
import com.tuniway.connect.repository.EmployeeProfileRepository;
import com.tuniway.connect.repository.TransportRepository;
import com.tuniway.connect.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final TransportRepository transportRepository;

    public AdminService(UserRepository userRepository, EmployeeProfileRepository employeeProfileRepository, TransportRepository transportRepository) {
        this.userRepository = userRepository;
        this.employeeProfileRepository = employeeProfileRepository;
        this.transportRepository = transportRepository;
    }

    public UpdatedEmployeeResponse deleteEmployee(UUID employeeId) {
        User user = userRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        employeeProfileRepository.findByUserId(employeeId).ifPresent(employeeProfileRepository::delete);

        userRepository.delete(user);

        UpdatedEmployeeResponse response = new UpdatedEmployeeResponse();
        response.setSuccess(true);
        response.setMessage("Employee deleted successfully");
        return response;
    }

    public UpdatedEmployeeResponse changeEmployeeStatus(UUID employeeId, UpdatedEmployeeStatusRequest request) {
        User user = userRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        if (request.getStatus() == null || request.getStatus().isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }

        try {
            AccountStatus newStatus = AccountStatus.valueOf(request.getStatus().toUpperCase());
            user.setStatus(newStatus);
            userRepository.save(user);

            UpdatedEmployeeResponse response = new UpdatedEmployeeResponse();
            response.setSuccess(true);
            response.setMessage("Employee status updated successfully");
            return response;

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value. Allowed values are: ACTIVE, INACTIVE");
        }
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

    @Transactional
    public TransportResponse createTransport(CreateTransportRequest request) {
        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new IllegalArgumentException("Transport code is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Transport name is required");
        }
        if (request.getTransportType() == null) {
            throw new IllegalArgumentException("Transport type is required");
        }
        if (request.getRouteName() == null || request.getRouteName().isBlank()) {
            throw new IllegalArgumentException("Route name is required");
        }

        Transport transport = new Transport();
        transport.setId(UUID.randomUUID());
        transport.setCode(request.getCode());
        transport.setName(request.getName());
        transport.setType(request.getTransportType());
        transport.setRoute_name(request.getRouteName());
        transport.setStart_point(request.getStart_point());
        transport.setEnd_point(request.getEnd_point());
        transport.setOperating_zone(request.getOperating_zone());
        transport.setActive(request.getIs_active() != null ? request.getIs_active() : true);
        transport.setZone(request.getZone());

        Transport savedTransport = transportRepository.save(transport);

        return new TransportResponse(savedTransport.getCode(), savedTransport.getType().name() , "Transport created successfully", true);
    }
}
