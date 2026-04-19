package com.tuniway.connect.controller;

import com.tuniway.connect.model.dto.ClientAccountResponse;
import com.tuniway.connect.model.dto.ClientDashboardResponse;
import com.tuniway.connect.model.dto.ClientNearbyTransportsResponse;
import com.tuniway.connect.model.dto.ClientTicketHistoryResponse;
import com.tuniway.connect.model.dto.ClientTicketProductsResponse;
import com.tuniway.connect.model.dto.ClientTicketPurchaseResponse;
import com.tuniway.connect.model.dto.ClientTransportDeparturesResponse;
import com.tuniway.connect.model.dto.ClientTransportDetailsResponse;
import com.tuniway.connect.model.dto.ClientTransportSearchResponse;
import com.tuniway.connect.model.dto.ClientTransportStopsResponse;
import com.tuniway.connect.model.dto.PurchaseClientTicketRequest;
import com.tuniway.connect.model.dto.UpdateClientAccountRequest;
import com.tuniway.connect.model.entity.User;
import com.tuniway.connect.repository.UserRepository;
import com.tuniway.connect.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;
import java.util.UUID;

// Initial implementation of ClientController to handle client-related endpoints such as retrieving dashboard information, managing account details, searching for transports, and purchasing tickets. Each endpoint is designed to handle specific client operations and return appropriate responses based on the success or failure of the operations. The endpoints are secured with role-based access control to ensure that only authenticated clients can access them.
@RestController
@RequestMapping("/api/v1/client")
public class ClientController { // Controller to handle client-related endpoints such as retrieving dashboard information, managing account details, searching for transports, and purchasing tickets. Each endpoint is designed to handle specific client operations and return appropriate responses based on the success or failure of the operations. The endpoints are secured with role-based access control to ensure that only authenticated clients can access them.

    @Autowired
    private ClientService clientService;

    @Autowired
    private UserRepository userRepository;

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/dashboard")
    public ResponseEntity<ClientDashboardResponse> getDashboard(Principal principal) { // Endpoint to retrieve the client's dashboard information. The endpoint is secured with role-based access control to ensure that only authenticated clients can access it. The response is wrapped in a try-catch block to handle any potential runtime exceptions and return appropriate error messages in the response body.
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
    public ResponseEntity<ClientAccountResponse> getAccount(Principal principal) { // Endpoint to retrieve the client's account details. The endpoint is secured with role-based access control to ensure that only authenticated clients can access it. The response is wrapped in a try-catch block to handle any potential runtime exceptions and return appropriate error messages in the response body.
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
                                                               Principal principal) { // Endpoint to update the client's account details. The endpoint is secured with role-based access control to ensure that only authenticated clients can access it. The response is wrapped in a try-catch block to handle any potential runtime exceptions and return appropriate error messages in the response body.
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

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/tickets/products")
    public ResponseEntity<ClientTicketProductsResponse> getTicketProducts(@RequestParam UUID transportId,
                                                                          @RequestParam UUID fromStopId,
                                                                          @RequestParam UUID toStopId,
                                                                          Principal principal) { // Endpoint to search for available ticket products based on transport and stop information. The endpoint is secured with role-based access control to ensure that only authenticated clients can access it. The response is wrapped in a try-catch block to handle any potential runtime exceptions and return appropriate error messages in the response body.
        try {
            User user = requireAuthenticatedUser(principal);
            ClientTicketProductsResponse response = clientService.getTicketProducts(
                user.getId(),
                transportId,
                fromStopId,
                toStopId
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            ClientTicketProductsResponse errorResponse = new ClientTicketProductsResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/tickets/purchase")
    public ResponseEntity<ClientTicketPurchaseResponse> purchaseTicket(@RequestBody PurchaseClientTicketRequest request,
                                                                       Principal principal) { // Endpoint to purchase a ticket. The endpoint is secured with role-based access control to ensure that only authenticated clients can access it. The response is wrapped in a try-catch block to handle any potential runtime exceptions and return appropriate error messages in the response body.
        try {
            User user = requireAuthenticatedUser(principal);
            ClientTicketPurchaseResponse response = clientService.purchaseTicket(user.getId(), request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            ClientTicketPurchaseResponse errorResponse = new ClientTicketPurchaseResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/tickets/history")
    public ResponseEntity<ClientTicketHistoryResponse> getTicketHistory(@RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "20") int size,
                                                                        @RequestParam(defaultValue = "purchaseTime,desc") String sort,
                                                                        Principal principal) { // Endpoint to retrieve the client's ticket history. The endpoint is secured with role-based access control to ensure that only authenticated clients can access it. The response is wrapped in a try-catch block to handle any potential runtime exceptions and return appropriate error messages in the response body.
        try {
            User user = requireAuthenticatedUser(principal);
            ClientTicketHistoryResponse response = clientService.getTicketHistory(user.getId(), page, size, sort);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            ClientTicketHistoryResponse errorResponse = new ClientTicketHistoryResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/transports/search")
    public ResponseEntity<ClientTransportSearchResponse> searchTransports(
        @RequestParam(required = false) String q,
        @RequestParam(required = false) String zone,
        @RequestParam(required = false) String type,
        @RequestParam(required = false) Boolean active,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "name,asc") String sort
    ) {
        try {
            ClientTransportSearchResponse response = clientService.searchTransports(q, zone, type, active, page, size, sort);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            ClientTransportSearchResponse errorResponse = new ClientTransportSearchResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/transports/nearby")
    public ResponseEntity<ClientNearbyTransportsResponse> getNearbyTransports(
        @RequestParam double lat,
        @RequestParam double lng,
        @RequestParam(required = false, defaultValue = "500") Integer radiusMeters
    ) { // Endpoint to retrieve nearby transports based on the client's location. The endpoint is secured with role-based access control to ensure that only authenticated clients can access it. The response is wrapped in a try-catch block to handle any potential runtime exceptions and return appropriate error messages in the response body.
        try {
            ClientNearbyTransportsResponse response = clientService.getNearbyTransports(lat, lng, radiusMeters);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            ClientNearbyTransportsResponse errorResponse = new ClientNearbyTransportsResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/transports/{id}")
    public ResponseEntity<ClientTransportDetailsResponse> getTransport(@PathVariable("id") UUID transportId) { // Endpoint to retrieve detailed information about a specific transport. The endpoint is secured with role-based access control to ensure that only authenticated clients can access it. The response is wrapped in a try-catch block to handle any potential runtime exceptions and return appropriate error messages in the response body.
        try {
            ClientTransportDetailsResponse response = clientService.getTransport(transportId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            ClientTransportDetailsResponse errorResponse = new ClientTransportDetailsResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/transports/{id}/stops")
    public ResponseEntity<ClientTransportStopsResponse> getTransportStops(@PathVariable("id") UUID transportId) { // Endpoint to retrieve the stops associated with a specific transport. The endpoint is secured with role-based access control to ensure that only authenticated clients can access it. The response is wrapped in a try-catch block to handle any potential runtime exceptions and return appropriate error messages in the response body.
        try {
            ClientTransportStopsResponse response = clientService.getTransportStops(transportId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            ClientTransportStopsResponse errorResponse = new ClientTransportStopsResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            errorResponse.setTransportId(transportId);
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/transports/{id}/departures")
    public ResponseEntity<ClientTransportDeparturesResponse> getTransportDepartures(@PathVariable("id") UUID transportId,
                                                                                    @RequestParam LocalDate date) { // Endpoint to retrieve the departure times for a specific transport on a given date. The endpoint is secured with role-based access control to ensure that only authenticated clients can access it. The response is wrapped in a try-catch block to handle any potential runtime exceptions and return appropriate error messages in the response body.
        try {
            ClientTransportDeparturesResponse response = clientService.getTransportDepartures(transportId, date);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            ClientTransportDeparturesResponse errorResponse = new ClientTransportDeparturesResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            errorResponse.setTransportId(transportId);
            errorResponse.setDate(date);
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    private User requireAuthenticatedUser(Principal principal) { // Helper method to retrieve the authenticated user based on the provided Principal. If the Principal is null or does not contain a valid email, a RuntimeException is thrown indicating that the request is unauthenticated. If the user is not found in the database, a RuntimeException is thrown indicating that the user was not found.
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new RuntimeException("Unauthenticated request");
        }

        return userRepository.findByEmail(principal.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
