package com.tuniway.connect.service;

import com.tuniway.connect.model.dto.RegisterClientRequest;
import com.tuniway.connect.model.dto.RegisterClientResponse;
import com.tuniway.connect.model.dto.LoginRequest;
import com.tuniway.connect.model.dto.LoginResponse;
import com.tuniway.connect.model.dto.VerifyEmailRequest;
import com.tuniway.connect.model.dto.VerifyEmailResponse;
import com.tuniway.connect.model.entity.AccountStatus;
import com.tuniway.connect.model.entity.ClientProfile;
import com.tuniway.connect.model.entity.EmailVerificationCode;
import com.tuniway.connect.model.entity.Role;
import com.tuniway.connect.model.entity.User;
import com.tuniway.connect.repository.ClientProfileRepository;
import com.tuniway.connect.repository.EmailVerificationCodeRepository;
import com.tuniway.connect.repository.UserRepository;

import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final EmailVerificationCodeRepository emailVerificationCodeRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final EmailService emailService;

    public UserService(
            UserRepository userRepository,
            EmailVerificationCodeRepository emailVerificationCodeRepository,
            ClientProfileRepository clientProfileRepository,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.emailVerificationCodeRepository = emailVerificationCodeRepository;
        this.clientProfileRepository = clientProfileRepository;
        this.emailService = emailService;
    }

    public RegisterClientResponse registerClient(RegisterClientRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }

        if (request.getPassword_hash() == null || request.getPassword_hash().isBlank()) {
            throw new RuntimeException("password_hash is required");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Validate username uniqueness if provided
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            if (clientProfileRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new RuntimeException("Username already exists");
            }
        }

        User user = new User();
        UUID uuid = UUID.randomUUID();
        user.setId(uuid);
        user.setEmail(request.getEmail());
        user.setPassword_hash(request.getPassword_hash());
        user.setRole(Role.CLIENT);
        user.setStatus(AccountStatus.INACTIVE);

        User savedUser = userRepository.save(user);

        // Create client profile
        ClientProfile profile = new ClientProfile();
        profile.setUserId(savedUser.getId());
        profile.setUsername(request.getUsername());
        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setPhone(request.getPhone());
        profile.setBirthDate(request.getBirthDate());
        clientProfileRepository.save(profile);

        String verificationCodeValue = generateVerificationCode();
        EmailVerificationCode verificationCode = new EmailVerificationCode();
        verificationCode.setEmail(savedUser.getEmail());
        verificationCode.setCode(verificationCodeValue);
        verificationCode.setExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES));
        verificationCode.setConsumed(false);
        emailVerificationCodeRepository.save(verificationCode);

        try {
            emailService.sendVerificationCode(savedUser.getEmail(), verificationCodeValue);
        } catch (MailException e) {
            throw new RuntimeException("Failed to send verification email: " + e.getMessage());
        }
    
        RegisterClientResponse response = new RegisterClientResponse();
        response.setId(savedUser.getId());
        response.setEmail(savedUser.getEmail());
        response.setRole(savedUser.getRole());
        response.setStatus(savedUser.getStatus());
        response.setCreatedAt(savedUser.getCreatedAt());
        response.setUsername(profile.getUsername());
        response.setFirstName(profile.getFirstName());
        response.setLastName(profile.getLastName());
        response.setPhone(profile.getPhone());
        response.setBirthDate(profile.getBirthDate());
        response.setMessage("Client registered successfully. Verify your email before login.");
        return response;
    }

    public VerifyEmailResponse verifyEmail(VerifyEmailRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }

        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new RuntimeException("Code is required");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        EmailVerificationCode verificationCode = emailVerificationCodeRepository
            .findLatestPendingByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("No pending verification code found"));

        if (!verificationCode.getCode().equals(request.getCode())) {
            throw new RuntimeException("Invalid verification code");
        }

        if (verificationCode.getExpiresAt() != null && verificationCode.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Verification code has expired");
        }

        verificationCode.setConsumed(true);
        emailVerificationCodeRepository.save(verificationCode);

        if (user.getStatus() != AccountStatus.ACTIVE) {
            user.setStatus(AccountStatus.ACTIVE);
            userRepository.save(user);
        }

        VerifyEmailResponse response = new VerifyEmailResponse();
        response.setEmail(user.getEmail());
        response.setVerified(true);
        response.setMessage("Email verified successfully");
        return response;
    }

    public LoginResponse login(LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new RuntimeException("Email is required");
        }

        if (request.getPassword_hash() == null || request.getPassword_hash().isBlank()) {
            throw new RuntimeException("password_hash is required");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!user.getPassword_hash().equals(request.getPassword_hash())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("Account is inactive");
        }

        user.setLastLoginAt(Instant.now());
        User updatedUser = userRepository.save(user);

        LoginResponse response = new LoginResponse();
        response.setId(updatedUser.getId());
        response.setEmail(updatedUser.getEmail());
        response.setRole(updatedUser.getRole());
        response.setStatus(updatedUser.getStatus());
        response.setLastLoginAt(updatedUser.getLastLoginAt());
        response.setAuthenticated(true);
        response.setMessage("Login successful");
        return response;
    }

    private String generateVerificationCode() {
        int code = new Random().nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
