package com.tuniway.connect.controller;

import com.tuniway.connect.model.dto.ClientAccountResponse;
import com.tuniway.connect.model.dto.ClientDashboardResponse;
import com.tuniway.connect.model.dto.UpdateClientAccountRequest;
import com.tuniway.connect.model.entity.User;
import com.tuniway.connect.repository.UserRepository;
import com.tuniway.connect.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/client")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @Autowired
    private UserRepository userRepository;

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/dashboard")
    public ResponseEntity<ClientDashboardResponse> getDashboard(Principal principal) {
        try {
            User user = requireAuthenticatedUser(principal);
            ClientDashboardResponse response = clientService.getDashboard(user.getId());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            ClientDashboardResponse errorResponse = new ClientDashboardResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/account")
    public ResponseEntity<ClientAccountResponse> getAccount(Principal principal) {
        try {
            User user = requireAuthenticatedUser(principal);
            ClientAccountResponse response = clientService.getAccount(user.getId());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            ClientAccountResponse errorResponse = new ClientAccountResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PatchMapping("/account")
    public ResponseEntity<ClientAccountResponse> updateAccount(@RequestBody UpdateClientAccountRequest request,
                                                               Principal principal) {
        try {
            User user = requireAuthenticatedUser(principal);
            ClientAccountResponse response = clientService.updateAccount(user.getId(), request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            ClientAccountResponse errorResponse = new ClientAccountResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
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
}
