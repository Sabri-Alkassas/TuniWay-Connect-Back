package com.tuniway.connect.controller;

import com.tuniway.connect.model.dto.RegisterClientRequest;
import com.tuniway.connect.model.dto.RegisterClientResponse;
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
}