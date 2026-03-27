package com.tuniway.connect.controller;

import com.tuniway.connect.model.dto.RegisterEmployeeRequest;
import com.tuniway.connect.model.dto.RegisterEmployeeResponse;
import com.tuniway.connect.model.dto.TransportResponse;
import com.tuniway.connect.model.dto.UpdateTransportRequest;
import com.tuniway.connect.model.dto.UpdatedEmployeeRequest;
import com.tuniway.connect.model.dto.UpdatedEmployeeResponse;
import com.tuniway.connect.model.dto.UpdatedEmployeeStatusRequest;
import com.tuniway.connect.service.AdminService;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

        } catch (RuntimeException e) {
            RegisterEmployeeResponse errorResponse = new RegisterEmployeeResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/staff-accounts/{id}")
    public ResponseEntity<UpdatedEmployeeResponse> updateStaffAccount(@RequestBody UpdatedEmployeeRequest request, @PathVariable("id") UUID employeeId) {
        try {
            UpdatedEmployeeResponse response = adminService.updateEmployee(employeeId, request);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (RuntimeException e) {
            UpdatedEmployeeResponse errorResponse = new UpdatedEmployeeResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/staff-accounts/{id}/status")
    public ResponseEntity<UpdatedEmployeeResponse> changeStaffAccountStatus(@RequestBody UpdatedEmployeeStatusRequest request, @PathVariable("id") UUID employeeId) {
        try {
            UpdatedEmployeeResponse response = adminService.changeEmployeeStatus(employeeId, request);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (RuntimeException e) {
            UpdatedEmployeeResponse errorResponse = new UpdatedEmployeeResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/staff-accounts/{id}")
    public ResponseEntity<UpdatedEmployeeResponse> deleteStaffAccount(@PathVariable("id") UUID employeeId) {
        try {
            UpdatedEmployeeResponse response = adminService.deleteEmployee(employeeId);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (RuntimeException e) {
            UpdatedEmployeeResponse errorResponse = new UpdatedEmployeeResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/transports/{id}")
    public ResponseEntity<TransportResponse> updateTransport(@RequestBody UpdateTransportRequest request, @PathVariable("id") UUID transportId) {
        try {
            TransportResponse response = adminService.updateTransport(transportId, request);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (RuntimeException e) {
            TransportResponse errorResponse = new TransportResponse(e.getMessage(), false);
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }
}
