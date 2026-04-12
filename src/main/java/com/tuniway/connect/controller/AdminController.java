package com.tuniway.connect.controller;

import com.tuniway.connect.model.dto.AdminDashboardResponse;
import com.tuniway.connect.model.dto.AdminShiftResponse;
import com.tuniway.connect.model.dto.CreateTransportRequest;
import com.tuniway.connect.model.dto.PlanningPublishRequest;
import com.tuniway.connect.model.dto.PlanningPublishResponse;
import com.tuniway.connect.model.dto.ReassignTransportRequest;
import com.tuniway.connect.model.dto.RegisterEmployeeRequest;
import com.tuniway.connect.model.dto.RegisterEmployeeResponse;
import com.tuniway.connect.model.dto.TransportResponse;
import com.tuniway.connect.model.dto.UpdateShiftRequest;
import com.tuniway.connect.model.dto.UpdateTransportDeparturesRequest;
import com.tuniway.connect.model.dto.UpdateTransportRequest;
import com.tuniway.connect.model.dto.UpdateTransportRouteRequest;
import com.tuniway.connect.model.dto.UpdateTransportStopsRequest;
import com.tuniway.connect.model.dto.UpdateTransportZoneRequest;
import com.tuniway.connect.model.dto.UpdatedEmployeeRequest;
import com.tuniway.connect.model.dto.UpdatedEmployeeResponse;
import com.tuniway.connect.model.dto.UpdatedEmployeeStatusRequest;
import com.tuniway.connect.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        try {
            return new ResponseEntity<>(adminService.getDashboard(), HttpStatus.OK);
        } catch (RuntimeException e) {
            AdminDashboardResponse errorResponse = new AdminDashboardResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/staff-accounts")
    public ResponseEntity<java.util.List<RegisterEmployeeResponse>> listStaffAccounts() {
        return new ResponseEntity<>(adminService.listStaffAccounts(), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/transports")
    public ResponseEntity<java.util.List<TransportResponse>> listTransports() {
        return new ResponseEntity<>(adminService.listTransports(), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/shifts")
    public ResponseEntity<java.util.List<AdminShiftResponse>> listShifts() {
        return new ResponseEntity<>(adminService.listShifts(), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/staff-accounts")
    public ResponseEntity<RegisterEmployeeResponse> createStaffAccount(@RequestBody RegisterEmployeeRequest request) {
        try {
            RegisterEmployeeResponse response = adminService.createStaffAccount(request);
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
    public ResponseEntity<UpdatedEmployeeResponse> updateStaffAccount(
        @RequestBody UpdatedEmployeeRequest request,
        @PathVariable("id") UUID staffId
    ) {
        try {
            UpdatedEmployeeResponse response = adminService.updateStaffAccount(staffId, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            UpdatedEmployeeResponse errorResponse = new UpdatedEmployeeResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @RequestMapping(path = "/staff-accounts/{id}/status", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ResponseEntity<UpdatedEmployeeResponse> changeStaffAccountStatus(
        @RequestBody UpdatedEmployeeStatusRequest request,
        @PathVariable("id") UUID staffId
    ) {
        try {
            UpdatedEmployeeResponse response = adminService.changeStaffAccountStatus(staffId, request);
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
    public ResponseEntity<UpdatedEmployeeResponse> deleteStaffAccount(@PathVariable("id") UUID staffId) {
        try {
            UpdatedEmployeeResponse response = adminService.deleteStaffAccount(staffId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            UpdatedEmployeeResponse errorResponse = new UpdatedEmployeeResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/transports/{id}/route")
    public ResponseEntity<TransportResponse> updateTransportRoute(
        @RequestBody UpdateTransportRouteRequest request,
        @PathVariable("id") UUID transportId
    ) {
        try {
            TransportResponse response = adminService.updateTransportRoute(transportId, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            TransportResponse errorResponse = new TransportResponse(e.getMessage(), false);
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/transports/{id}/zone")
    public ResponseEntity<TransportResponse> updateTransportZone(
        @RequestBody UpdateTransportZoneRequest request,
        @PathVariable("id") UUID transportId
    ) {
        try {
            TransportResponse response = adminService.updateTransportZone(transportId, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            TransportResponse errorResponse = new TransportResponse(e.getMessage(), false);
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/transports/{id}/stops")
    public ResponseEntity<TransportResponse> updateTransportStops(
        @RequestBody UpdateTransportStopsRequest request,
        @PathVariable("id") UUID transportId
    ) {
        try {
            TransportResponse response = adminService.updateTransportStops(transportId, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            TransportResponse errorResponse = new TransportResponse(e.getMessage(), false);
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/transports/{id}/departures")
    public ResponseEntity<TransportResponse> updateTransportDepartures(
        @RequestBody UpdateTransportDeparturesRequest request,
        @PathVariable("id") UUID transportId
    ) {
        try {
            TransportResponse response = adminService.updateTransportDepartures(transportId, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            TransportResponse errorResponse = new TransportResponse(e.getMessage(), false);
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/transports/{id}")
    public ResponseEntity<TransportResponse> updateTransport(
        @RequestBody UpdateTransportRequest request,
        @PathVariable("id") UUID transportId
    ) {
        try {
            TransportResponse response = adminService.updateTransport(transportId, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            TransportResponse errorResponse = new TransportResponse(e.getMessage(), false);
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/transports")
    public ResponseEntity<TransportResponse> createTransports(@RequestBody CreateTransportRequest request) {
        try {
            TransportResponse response = adminService.createTransport(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            TransportResponse errorResponse = new TransportResponse(e.getMessage(), false);
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/shifts/{id}")
    public ResponseEntity<AdminShiftResponse> updateShift(
        @RequestBody UpdateShiftRequest request,
        @PathVariable("id") UUID shiftId
    ) {
        try {
            AdminShiftResponse response = adminService.updateShift(shiftId, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            AdminShiftResponse errorResponse = new AdminShiftResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            errorResponse.setShiftId(shiftId);
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/shifts/{id}/reassign-transport")
    public ResponseEntity<AdminShiftResponse> reassignShiftTransport(
        @RequestBody ReassignTransportRequest request,
        @PathVariable("id") UUID shiftId
    ) {
        try {
            AdminShiftResponse response = adminService.reassignEmployeeTransport(shiftId, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            AdminShiftResponse errorResponse = new AdminShiftResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            errorResponse.setShiftId(shiftId);
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/planning/publish")
    public ResponseEntity<PlanningPublishResponse> publishPlanningChanges(@RequestBody PlanningPublishRequest request) {
        try {
            PlanningPublishResponse response = adminService.publishPlanningChanges(request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            PlanningPublishResponse errorResponse = new PlanningPublishResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }
}
