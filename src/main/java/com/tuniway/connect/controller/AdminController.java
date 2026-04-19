package com.tuniway.connect.controller;

import com.tuniway.connect.model.dto.AdminDashboardResponse;
import com.tuniway.connect.model.dto.AdminShiftResponse;
import com.tuniway.connect.model.dto.CreateShiftRequest;
import com.tuniway.connect.model.dto.CreateTransportRequest;
import com.tuniway.connect.model.dto.PlanningPublishRequest;
import com.tuniway.connect.model.dto.PlanningPublishResponse;
import com.tuniway.connect.model.dto.ReassignTransportRequest;
import com.tuniway.connect.model.dto.RegisterEmployeeRequest;
import com.tuniway.connect.model.dto.RegisterEmployeeResponse;
import com.tuniway.connect.model.dto.TransportDeparturesResponse;
import com.tuniway.connect.model.dto.TransportResponse;
import com.tuniway.connect.model.dto.TransportStopItem;
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

// AdminController handles all administrative operations related to managing staff accounts, transport routes, shifts, and planning. It provides endpoints for creating, updating, and deleting staff accounts, as well as managing transport details and shift assignments. All endpoints are secured with role-based access control to ensure that only users with the ADMIN role can access these functionalities.
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard() { // Endpoint to retrieve dashboard data for the admin panel, including key metrics and summaries. The response is wrapped in a try-catch block to handle any potential runtime exceptions and return appropriate error messages in the response body.
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
    public ResponseEntity<java.util.List<RegisterEmployeeResponse>> listStaffAccounts() { // Endpoint to retrieve a list of all staff accounts. The response is wrapped in a try-catch block to handle any potential runtime exceptions and return appropriate error messages in the response body.
        return new ResponseEntity<>(adminService.listStaffAccounts(), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/transports")
    public ResponseEntity<java.util.List<TransportResponse>> listTransports() { // Endpoint to retrieve a list of all transports. The response is wrapped in a try-catch block to handle any potential runtime exceptions and return appropriate error messages in the response body.
        return new ResponseEntity<>(adminService.listTransports(), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stops")
    public ResponseEntity<java.util.List<TransportStopItem>> listStops() { // Endpoint to retrieve a list of all stops. The response is wrapped in a try-catch block to handle any potential runtime exceptions and return appropriate error messages in the response body.
        return new ResponseEntity<>(adminService.listStops(), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/transports/{id}/stops")
    public ResponseEntity<UpdateTransportStopsRequest> getTransportStops(@PathVariable("id") UUID transportId) { // Endpoint to retrieve the stops for a specific transport. The response is wrapped in a try-catch block to handle any potential runtime exceptions and return appropriate error messages in the response body.
        return new ResponseEntity<>(adminService.getTransportStops(transportId), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/transports/{id}/departures")
    public ResponseEntity<TransportDeparturesResponse> getTransportDepartures(@PathVariable("id") UUID transportId) { // Endpoint to retrieve the departures for a specific transport. The response is wrapped in a try-catch block to handle any potential runtime exceptions and return appropriate error messages in the response body.
        return new ResponseEntity<>(adminService.getTransportDepartures(transportId), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/shifts")
    public ResponseEntity<java.util.List<AdminShiftResponse>> listShifts() { // Endpoint to retrieve a list of all shifts. The response is wrapped in a try-catch block to handle any potential runtime exceptions and return appropriate error messages in the response body.
        return new ResponseEntity<>(adminService.listShifts(), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/shifts")
    public ResponseEntity<AdminShiftResponse> createShift(@RequestBody CreateShiftRequest request) { // Endpoint to create a new shift. The request body contains the details of the shift to be created. The response is wrapped in a try-catch block to handle any potential runtime exceptions and return appropriate error messages in the response body.
        try {
            AdminShiftResponse response = adminService.createShift(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            AdminShiftResponse errorResponse = new AdminShiftResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/staff-accounts")
    public ResponseEntity<RegisterEmployeeResponse> createStaffAccount(@RequestBody RegisterEmployeeRequest request) { // Endpoint to create a new staff account. The request body contains the details of the staff member to be created. The response is wrapped in a try-catch block to handle any potential runtime exceptions and return appropriate error messages in the response body.
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
    ) { // Endpoint to update an existing staff account. The request body contains the updated details of the staff member. The response is wrapped in a try-catch block to handle any potential runtime exceptions and return appropriate error messages in the response body.
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
    ) { // Endpoint to change the status of a staff account. The request body contains the updated status information. The response is wrapped in a try-catch block to handle any potential runtime exceptions and return appropriate error messages in the response body.
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
    public ResponseEntity<UpdatedEmployeeResponse> deleteStaffAccount(@PathVariable("id") UUID staffId) { // Endpoint to delete a staff account. The response is wrapped in a try-catch block to handle any potential runtime exceptions and return appropriate error messages in the response body.
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
    ) { // Endpoint to update the route of a specific transport. The request body contains the updated route information. The response is wrapped in a try-catch block to handle any potential runtime exceptions and return appropriate error messages in the response body.
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
    ) { // Endpoint to update the zone of a specific transport. The request body contains the updated zone information. The response is wrapped in a try-catch block to handle any potential runtime exceptions and return appropriate error messages in the response body.
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
    ) { // Endpoint to update the stops of a specific transport. The request body contains the updated stops information. The response is wrapped in a try-catch block to handle any potential runtime exceptions and return appropriate error messages in the response body.
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
    ) { // Endpoint to update the departures of a specific transport. The request body contains the updated departures information. The response is wrapped in a try-catch block to handle any potential runtime exceptions and return appropriate error messages in the response body.
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
    ) { // Endpoint to update the details of a specific transport. The request body contains the updated transport information. The response is wrapped in a try-catch block to handle any potential runtime exceptions and return appropriate error messages in the response body.
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
        try { // Endpoint to create a new transport. The request body contains the details of the transport to be created. The response is wrapped in a try-catch block to handle any potential runtime exceptions and return appropriate error messages in the response body.
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
    ) { // Endpoint to update the details of a specific shift. The request body contains the updated shift information. The response is wrapped in a try-catch block to handle any potential runtime exceptions and return appropriate error messages in the response body.
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
    ) { // Endpoint to reassign the transport of a specific shift. The request body contains the updated transport information. The response is wrapped in a try-catch block to handle any potential runtime exceptions and return appropriate error messages in the response body.
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
    public ResponseEntity<PlanningPublishResponse> publishPlanningChanges(@RequestBody PlanningPublishRequest request) { // Endpoint to publish planning changes. The request body contains the details of the planning changes to be published. The response is wrapped in a try-catch block to handle any potential runtime exceptions and return appropriate error messages in the response body.
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
