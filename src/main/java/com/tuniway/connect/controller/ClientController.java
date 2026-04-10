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

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/tickets/products")
    public ResponseEntity<ClientTicketProductsResponse> getTicketProducts(@RequestParam UUID transportId,
                                                                          @RequestParam UUID fromStopId,
                                                                          @RequestParam UUID toStopId,
                                                                          Principal principal) {
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
                                                                       Principal principal) {
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
                                                                        Principal principal) {
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
    ) {
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
    public ResponseEntity<ClientTransportDetailsResponse> getTransport(@PathVariable("id") UUID transportId) {
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
    public ResponseEntity<ClientTransportStopsResponse> getTransportStops(@PathVariable("id") UUID transportId) {
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
                                                                                    @RequestParam LocalDate date) {
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

    private User requireAuthenticatedUser(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new RuntimeException("Unauthenticated request");
        }

        return userRepository.findByEmail(principal.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
