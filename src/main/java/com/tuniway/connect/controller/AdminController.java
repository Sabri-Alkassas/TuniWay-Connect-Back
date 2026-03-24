package com.tuniway.connect.controller;

import com.tuniway.connect.model.dto.RegisterEmployeeRequest;
import com.tuniway.connect.model.dto.RegisterEmployeeResponse;
import com.tuniway.connect.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping ("/api/v1/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/staff-accounts")
    public ResponseEntity<RegisterEmployeeResponse> createStaffAccount(@RequestBody RegisterEmployeeRequest request) {
        try {
            RegisterEmployeeResponse response = adminService.registerEmployee(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (Exception e) {
            RegisterEmployeeResponse errorResponse = new RegisterEmployeeResponse();
            errorResponse.setMessage(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }
    

}
