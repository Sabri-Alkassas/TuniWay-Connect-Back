package com.tuniway.connect.service;

import com.tuniway.connect.model.dto.RegisterClientRequest;
import com.tuniway.connect.model.dto.RegisterClientResponse;
import com.tuniway.connect.model.dto.LoginRequest;
import com.tuniway.connect.model.dto.LoginResponse;
import com.tuniway.connect.model.dto.VerifyEmailRequest;
import com.tuniway.connect.model.dto.VerifyEmailResponse;
import com.tuniway.connect.model.dto.VerifyTwoFactorRequest;
import com.tuniway.connect.model.dto.VerifyTwoFactorResponse;
import com.tuniway.connect.model.dto.RefreshRequest;
import com.tuniway.connect.model.dto.RefreshResponse;
import com.tuniway.connect.model.dto.LogoutRequest;
import com.tuniway.connect.model.dto.LogoutResponse;
import com.tuniway.connect.model.entity.AccountStatus;
import com.tuniway.connect.model.entity.AdminProfile;
import com.tuniway.connect.model.entity.ClientProfile;
import com.tuniway.connect.model.entity.EmailVerificationCode;
import com.tuniway.connect.model.entity.EmployeeProfile;
import com.tuniway.connect.model.entity.RefreshToken;
import com.tuniway.connect.model.entity.Role;
import com.tuniway.connect.model.entity.User;
import com.tuniway.connect.repository.AdminProfileRepository;
import com.tuniway.connect.repository.ClientProfileRepository;
import com.tuniway.connect.repository.EmailVerificationCodeRepository;
import com.tuniway.connect.repository.EmployeeProfileRepository;
import com.tuniway.connect.repository.RefreshTokenRepository;
import com.tuniway.connect.repository.UserRepository;

import jakarta.transaction.Transactional;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final EmailVerificationCodeRepository emailVerificationCodeRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final AdminProfileRepository adminProfileRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailService emailService;
    private final TwoFactorChallengeService twoFactorChallengeService;
    private final TotpService totpService;
    private final JwtService jwtService;

    public UserService(
            UserRepository userRepository,
            EmailVerificationCodeRepository emailVerificationCodeRepository,
            ClientProfileRepository clientProfileRepository,
            AdminProfileRepository adminProfileRepository,
            EmployeeProfileRepository employeeProfileRepository,
            RefreshTokenRepository refreshTokenRepository,
            EmailService emailService,
            TwoFactorChallengeService twoFactorChallengeService,
            TotpService totpService,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.emailVerificationCodeRepository = emailVerificationCodeRepository;
        this.clientProfileRepository = clientProfileRepository;
        this.adminProfileRepository = adminProfileRepository;
        this.employeeProfileRepository = employeeProfileRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.emailService = emailService;
        this.twoFactorChallengeService = twoFactorChallengeService;
        this.totpService = totpService;
        this.jwtService = jwtService;
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

    @Transactional
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

        if (requiresTwoFactor(user.getRole())) {
            String secret = loadStaffTwoFactorSecret(user);
            if (secret.isBlank()) {
                throw new RuntimeException("Two-factor secret is not configured");
            }

            String tempToken = twoFactorChallengeService.createChallenge(user.getId(), Duration.ofMinutes(5));

            LoginResponse response = new LoginResponse();
            response.setId(user.getId());
            response.setEmail(user.getEmail());
            response.setRole(user.getRole());
            response.setStatus(user.getStatus());
            response.setAuthenticated(false);
            response.setTwoFactorRequired(true);
            response.setTempToken(tempToken);
            response.setAccessToken(null);
            response.setRefreshToken(null);
            response.setMessage("2FA required. Verify using /api/v1/auth/2fa/verify");
            return response;
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
        response.setTwoFactorRequired(false);
        response.setAccessToken(jwtService.generateAccessToken(updatedUser));
        response.setRefreshToken(issueRefreshTokenForUser(updatedUser.getId()));
        response.setMessage("Login successful");
        return response;
    }

    @Transactional
    public VerifyTwoFactorResponse verifyTwoFactor(VerifyTwoFactorRequest request) {
        if (request.getTempToken() == null || request.getTempToken().isBlank()) {
            throw new RuntimeException("tempToken is required");
        }

        if (request.getTotpCode() == null || request.getTotpCode().isBlank()) {
            throw new RuntimeException("totpCode is required");
        }

        UUID userId = twoFactorChallengeService.consumeChallenge(request.getTempToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!requiresTwoFactor(user.getRole())) {
            throw new RuntimeException("2FA verification is only for staff accounts");
        }

        String secret = loadStaffTwoFactorSecret(user);
        if (!totpService.isValidCode(secret, request.getTotpCode())) {
            throw new RuntimeException("Invalid TOTP code");
        }

        user.setLastLoginAt(Instant.now());
        User updatedUser = userRepository.save(user);

        VerifyTwoFactorResponse response = new VerifyTwoFactorResponse();
        response.setId(updatedUser.getId());
        response.setEmail(updatedUser.getEmail());
        response.setRole(updatedUser.getRole());
        response.setStatus(updatedUser.getStatus());
        response.setLastLoginAt(updatedUser.getLastLoginAt());
        response.setAuthenticated(true);
        response.setAccessToken(jwtService.generateAccessToken(updatedUser));
        response.setRefreshToken(issueRefreshTokenForUser(updatedUser.getId()));
        response.setMessage("2FA verification successful");
        return response;
    }

    @Transactional
    public RefreshResponse refresh(RefreshRequest request) {
        if (request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            throw new RuntimeException("refreshToken is required");
        }

        String tokenHash = sha256Hex(request.getRefreshToken());
        Instant now = Instant.now();
        int updatedRows = refreshTokenRepository.revokeIfActive(tokenHash, now);
        if (updatedRows == 0) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        RefreshToken currentToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new RuntimeException("Refresh token record not found"));

        User user = userRepository.findById(currentToken.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("Account is inactive");
        }

        RefreshResponse response = new RefreshResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setLastLoginAt(user.getLastLoginAt());
        response.setAuthenticated(true);
        response.setAccessToken(jwtService.generateAccessToken(user));
        response.setRefreshToken(issueRefreshTokenForUser(user.getId()));
        response.setMessage("Token refreshed successfully");
        return response;
    }

    @Transactional
    public LogoutResponse logout(LogoutRequest request) {
        if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()) {
            String tokenHash = sha256Hex(request.getRefreshToken());
            refreshTokenRepository.revokeIfNotRevoked(tokenHash);
        }

        LogoutResponse response = new LogoutResponse();
        response.setSuccess(true);
        response.setMessage("Logout processed");
        return response;
    }

    private String generateVerificationCode() {
        int code = new Random().nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    private String issueRefreshTokenForUser(UUID userId) {
        String plainToken = UUID.randomUUID() + "." + UUID.randomUUID();

        Instant now = Instant.now();
        refreshTokenRepository.deleteByRevokedTrueOrExpiresAtBefore(now);
        refreshTokenRepository.deleteByUserId(userId);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setTokenHash(sha256Hex(plainToken));
        refreshToken.setExpiresAt(now.plus(30, ChronoUnit.DAYS));
        refreshToken.setRevoked(false);
        refreshToken.setCreatedAt(now);
        refreshTokenRepository.save(refreshToken);

        return plainToken;
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }

    private boolean requiresTwoFactor(Role role) {
        return role == Role.ADMIN || role == Role.EMPLOYEE;
    }

    private String loadStaffTwoFactorSecret(User user) {
        if (user.getRole() == Role.ADMIN) {
            AdminProfile profile = adminProfileRepository.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("Admin profile not found"));

            if (!Boolean.TRUE.equals(profile.getTwoFactorEnabled())) {
                throw new RuntimeException("2FA is not enabled for this admin account");
            }

            if (profile.getTwoFactorSecretEncrypted() == null || profile.getTwoFactorSecretEncrypted().isBlank()) {
                throw new RuntimeException("Admin 2FA secret is missing");
            }

            return profile.getTwoFactorSecretEncrypted();
        }

        EmployeeProfile profile = employeeProfileRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Employee profile not found"));

        if (!Boolean.TRUE.equals(profile.getTwoFactorEnabled())) {
            throw new RuntimeException("2FA is not enabled for this employee account");
        }

        if (profile.getTwoFactorSecretEncrypted() == null || profile.getTwoFactorSecretEncrypted().isBlank()) {
            throw new RuntimeException("Employee 2FA secret is missing");
        }

        return profile.getTwoFactorSecretEncrypted();
    }
}
