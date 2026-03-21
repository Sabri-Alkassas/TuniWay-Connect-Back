package com.tuniway.connect.controller;

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
import com.tuniway.connect.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register-client")
    public ResponseEntity<RegisterClientResponse> registerClient(@RequestBody RegisterClientRequest request) {
        try {
            RegisterClientResponse response = userService.registerClient(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            RegisterClientResponse errorResponse = new RegisterClientResponse();
            errorResponse.setMessage(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<VerifyEmailResponse> verifyEmail(@RequestBody VerifyEmailRequest request) {
        try {
            VerifyEmailResponse response = userService.verifyEmail(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            VerifyEmailResponse errorResponse = new VerifyEmailResponse();
            errorResponse.setVerified(false);
            errorResponse.setMessage(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.login(request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            LoginResponse errorResponse = new LoginResponse();
            errorResponse.setAuthenticated(false);
            errorResponse.setMessage(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/2fa/verify")
    public ResponseEntity<VerifyTwoFactorResponse> verifyTwoFactor(@RequestBody VerifyTwoFactorRequest request) {
        try {
            VerifyTwoFactorResponse response = userService.verifyTwoFactor(request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            VerifyTwoFactorResponse errorResponse = new VerifyTwoFactorResponse();
            errorResponse.setAuthenticated(false);
            errorResponse.setMessage(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponse> refresh(@RequestBody RefreshRequest request) {
        try {
            RefreshResponse response = userService.refresh(request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            RefreshResponse errorResponse = new RefreshResponse();
            errorResponse.setAuthenticated(false);
            errorResponse.setMessage(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }
}