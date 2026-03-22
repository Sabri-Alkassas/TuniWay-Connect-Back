package com.tuniway.connect.controller;

import com.tuniway.connect.model.dto.EmployeeScheduleResponse;
import com.tuniway.connect.model.dto.ShiftStartRequest;
import com.tuniway.connect.model.dto.ShiftStartResponse;
import com.tuniway.connect.model.entity.User;
import com.tuniway.connect.repository.UserRepository;
import com.tuniway.connect.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;



@RestController
@RequestMapping("/api/v1/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/schedule")
    public ResponseEntity<EmployeeScheduleResponse> getSchedule(Principal principal) {
        try {
            String email = principal.getName();
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
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
    public ResponseEntity<ShiftStartResponse> postShiftStartTime(@RequestBody ShiftStartRequest request, @PathVariable java.util.UUID id, Principal principal) {
        try {
            User user = requireAuthenticatedUser(principal);
            return new ResponseEntity<>(employeeService.startShift(request, id, user), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(new ShiftStartResponse() {{
                setSuccess(false);
                setMessage(e.getMessage());
            }}, HttpStatus.BAD_REQUEST);
        }
    }

    private User requireAuthenticatedUser(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new RuntimeException("Unauthenticated request");
        }

        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
