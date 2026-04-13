package com.tuniway.connect.controller;

import com.tuniway.connect.model.dto.EmployeeScheduleResponse;
import com.tuniway.connect.model.dto.EmployeeLocationUpdateRequest;
import com.tuniway.connect.model.dto.EmployeeShiftLocationResponse;
import com.tuniway.connect.model.dto.EmployeeShiftStopsResponse;
import com.tuniway.connect.model.dto.EmployeeStopActionResponse;
import com.tuniway.connect.model.dto.EmployeeShiftProgressResponse;
import com.tuniway.connect.model.dto.ShiftEndResponse;
import com.tuniway.connect.model.dto.ShiftStartRequest;
import com.tuniway.connect.model.dto.ShiftStartResponse;
import com.tuniway.connect.model.entity.User;
import com.tuniway.connect.repository.UserRepository;
import com.tuniway.connect.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private UserRepository userRepository;

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/schedule")
    public ResponseEntity<EmployeeScheduleResponse> getSchedule(Principal principal) {
        try {
            User user = requireAuthenticatedUser(principal);
            EmployeeScheduleResponse response = employeeService.getSchedule(user.getId());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            EmployeeScheduleResponse errorResponse = new EmployeeScheduleResponse();
            errorResponse.setMessage(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/shifts/{id}/start")
    public ResponseEntity<ShiftStartResponse> postShiftStartTime(@RequestBody(required = false) ShiftStartRequest request,
                                                                 @PathVariable("id") UUID shiftId,
                                                                 Principal principal) {
        try {
            User user = requireAuthenticatedUser(principal);
            return new ResponseEntity<>(employeeService.startShift(request, shiftId, user), HttpStatus.OK);
        } catch (RuntimeException e) {
            ShiftStartResponse errorResponse = new ShiftStartResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            errorResponse.setShiftId(shiftId);
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/shifts/{id}/end")
    public ResponseEntity<ShiftEndResponse> postShiftEndTime(@PathVariable("id") UUID shiftId, Principal principal) {
        try {
            User user = requireAuthenticatedUser(principal);
            return new ResponseEntity<>(employeeService.endShift(shiftId, user), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(new ShiftEndResponse() {{
                setSuccess(false);
                setMessage(e.getMessage());
            }}, HttpStatus.BAD_REQUEST);
        }
    }   

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/shifts/{id}/location")
    public ResponseEntity<EmployeeShiftLocationResponse> updateShiftLocation(@PathVariable("id") UUID shiftId,
                                                                             @RequestBody(required = false) EmployeeLocationUpdateRequest request,
                                                                             Principal principal) {
        try {
            User user = requireAuthenticatedUser(principal);
            EmployeeShiftLocationResponse response = employeeService.updateShiftLocation(user, shiftId, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            EmployeeShiftLocationResponse errorResponse = new EmployeeShiftLocationResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            errorResponse.setShiftId(shiftId);
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }
  
    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/shifts/{id}/stops")
    public ResponseEntity<EmployeeShiftStopsResponse> getShiftStops(@PathVariable("id") UUID shiftId,
                                                                     Principal principal) {
        try {
            User user = requireAuthenticatedUser(principal);
            EmployeeShiftStopsResponse response = employeeService.getShiftStops(user.getId(), shiftId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            EmployeeShiftStopsResponse errorResponse = new EmployeeShiftStopsResponse();
            errorResponse.setMessage(e.getMessage());
            errorResponse.setShiftId(shiftId.toString());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/shifts/{id}/stops/{stopId}/arrive")
    public ResponseEntity<EmployeeStopActionResponse> arriveStop(@PathVariable("id") UUID shiftId,
                                                                 @PathVariable UUID stopId,
                                                                 Principal principal) {
        try {
            User user = requireAuthenticatedUser(principal);
            EmployeeStopActionResponse response = employeeService.arriveAtStop(user, shiftId, stopId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            EmployeeStopActionResponse errorResponse = new EmployeeStopActionResponse();
            errorResponse.setMessage(e.getMessage());
            errorResponse.setShiftId(shiftId.toString());
            errorResponse.setStopId(stopId.toString());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/shifts/{id}/stops/{stopId}/depart")
    public ResponseEntity<EmployeeStopActionResponse> departStop(@PathVariable("id") UUID shiftId,
                                                                 @PathVariable UUID stopId,
                                                                 Principal principal) {
        try {
            User user = requireAuthenticatedUser(principal);
            EmployeeStopActionResponse response = employeeService.departFromStop(user, shiftId, stopId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            EmployeeStopActionResponse errorResponse = new EmployeeStopActionResponse();
            errorResponse.setMessage(e.getMessage());
            errorResponse.setShiftId(shiftId.toString());
            errorResponse.setStopId(stopId.toString());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    private User requireAuthenticatedUser(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new RuntimeException("Unauthenticated request");
        }

        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/shifts/{id}/progress")
    public ResponseEntity<EmployeeShiftProgressResponse> getShiftProgress(@PathVariable("id") UUID shiftId,
                                                                        Principal principal) {
        try {
            User user = requireAuthenticatedUser(principal);
            EmployeeShiftProgressResponse response = employeeService.getShiftProgress(user.getId(), shiftId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            EmployeeShiftProgressResponse errorResponse = new EmployeeShiftProgressResponse();
            errorResponse.setMessage(e.getMessage());
            errorResponse.setShiftId(shiftId.toString());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }
}
