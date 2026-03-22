package com.tuniway.connect.controller;

import com.tuniway.connect.model.dto.EmployeeScheduleResponse;
import com.tuniway.connect.model.entity.User;
import com.tuniway.connect.repository.UserRepository;
import com.tuniway.connect.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
}
