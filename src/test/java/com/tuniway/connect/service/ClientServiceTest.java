package com.tuniway.connect.service;

import com.tuniway.connect.model.dto.ClientAccountResponse;
import com.tuniway.connect.model.dto.ClientDashboardResponse;
import com.tuniway.connect.model.dto.ClientTransportDeparturesResponse;
import com.tuniway.connect.model.dto.UpdateClientAccountRequest;
import com.tuniway.connect.model.entity.AccountStatus;
import com.tuniway.connect.model.entity.ClientProfile;
import com.tuniway.connect.model.entity.Transport;
import com.tuniway.connect.model.entity.TransportDepartureSlot;
import com.tuniway.connect.model.entity.TransportRouteStop;
import com.tuniway.connect.model.entity.TransportStop;
import com.tuniway.connect.model.entity.TransportType;
import com.tuniway.connect.model.entity.User;
import com.tuniway.connect.repository.ClientProfileRepository;
import com.tuniway.connect.repository.EmailVerificationCodeRepository;
import com.tuniway.connect.repository.PaymentTransactionRepository;
import com.tuniway.connect.repository.ShiftStopEventRepository;
import com.tuniway.connect.repository.TicketFareRuleRepository;
import com.tuniway.connect.repository.TicketProductRepository;
import com.tuniway.connect.repository.TicketPurchaseRepository;
import com.tuniway.connect.repository.TransportDepartureSlotRepository;
import com.tuniway.connect.repository.TransportFareProfileRepository;
import com.tuniway.connect.repository.TransportRepository;
import com.tuniway.connect.repository.TransportRouteStopRepository;
import com.tuniway.connect.repository.UserRepository;
import com.tuniway.connect.repository.WorkShiftRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClientProfileRepository clientProfileRepository;

    @Mock
    private EmailVerificationCodeRepository emailVerificationCodeRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private TransportRepository transportRepository;

    @Mock
    private TransportRouteStopRepository transportRouteStopRepository;

    @Mock
    private TransportDepartureSlotRepository transportDepartureSlotRepository;

    @Mock
    private TransportFareProfileRepository transportFareProfileRepository;

    @Mock
    private TicketFareRuleRepository ticketFareRuleRepository;

    @Mock
    private TicketProductRepository ticketProductRepository;

    @Mock
    private TicketPurchaseRepository ticketPurchaseRepository;

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;

    @Mock
    private ShiftStopEventRepository shiftStopEventRepository;

    @Mock
    private WorkShiftRepository workShiftRepository;

    @InjectMocks
    private ClientService clientService;

    @Test
    void getDashboardBuildsClientSummaryFromPersistedData() {
        UUID clientId = UUID.randomUUID();
        User user = buildClientUser(clientId, "client@test.com");
        user.setLastLoginAt(Instant.parse("2026-04-09T10:15:30Z"));

        ClientProfile profile = new ClientProfile();
        profile.setUserId(clientId);
        profile.setUsername("clientuser");
        profile.setFirstName("Tuni");
        profile.setLastName("Way");

        when(userRepository.findById(clientId)).thenReturn(Optional.of(user));
        when(clientProfileRepository.findById(clientId)).thenReturn(Optional.of(profile));
        when(ticketPurchaseRepository.findTop3ByUserIdOrderByPurchaseTimeDesc(clientId)).thenReturn(List.of());
        when(ticketPurchaseRepository.countByUserId(clientId)).thenReturn(0L);
        when(ticketPurchaseRepository.countByUserIdAndStatusIgnoreCaseAndValidUntilAfter(eq(clientId), eq("ACTIVE"), any(Instant.class)))
            .thenReturn(0L);

        ClientDashboardResponse response = clientService.getDashboard(clientId);

        assertTrue(response.isSuccess());
        assertEquals(clientId, response.getClientId());
        assertEquals("Tuni Way", response.getDisplayName());
        assertFalse(response.isProfileComplete());
        assertEquals(1, response.getMissingProfileFields().size());
        assertEquals("birthDate", response.getMissingProfileFields().get(0));
    }

    @Test
    void updateAccountPersistsChangedFields() {
        UUID clientId = UUID.randomUUID();
        User user = buildClientUser(clientId, "old@test.com");
        ClientProfile profile = new ClientProfile();
        profile.setUserId(clientId);
        profile.setUsername("olduser");
        profile.setFirstName("Old");
        profile.setLastName("Name");

        UpdateClientAccountRequest request = new UpdateClientAccountRequest();
        request.setEmail("new@test.com");
        request.setUsername("newuser");
        request.setFirstName("New");
        request.setPhone("12345678");
        request.setBirthDate(Instant.parse("2000-01-01T00:00:00Z"));

        when(userRepository.findById(clientId)).thenReturn(Optional.of(user));
        when(clientProfileRepository.findById(clientId)).thenReturn(Optional.of(profile));
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(clientProfileRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);
        when(clientProfileRepository.save(profile)).thenReturn(profile);

        ClientAccountResponse response = clientService.updateAccount(clientId, request);

        assertTrue(response.isSuccess());
        assertEquals("new@test.com", response.getEmail());
        assertEquals("newuser", response.getUsername());
        assertEquals("New", response.getFirstName());
        assertEquals("12345678", response.getPhone());
        assertEquals(Instant.parse("2000-01-01T00:00:00Z"), response.getBirthDate());
        verify(userRepository).save(user);
        verify(clientProfileRepository).save(profile);
    }

    @Test
    void updateAccountRejectsDuplicateUsernameOwnedByAnotherClient() {
        UUID clientId = UUID.randomUUID();
        User user = buildClientUser(clientId, "client@test.com");
        ClientProfile profile = new ClientProfile();
        profile.setUserId(clientId);
        profile.setUsername("currentuser");

        ClientProfile existingProfile = new ClientProfile();
        existingProfile.setUserId(UUID.randomUUID());
        existingProfile.setUsername("takenuser");

        UpdateClientAccountRequest request = new UpdateClientAccountRequest();
        request.setUsername("takenuser");

        when(userRepository.findById(clientId)).thenReturn(Optional.of(user));
        when(clientProfileRepository.findById(clientId)).thenReturn(Optional.of(profile));
        when(clientProfileRepository.findByUsername("takenuser")).thenReturn(Optional.of(existingProfile));

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> clientService.updateAccount(clientId, request)
        );

        assertEquals("A client with this username already exists", exception.getMessage());
        verify(userRepository, never()).save(user);
        verify(clientProfileRepository, never()).save(profile);
    }

    @Test
    void getTransportDeparturesUsesObservedAnchorsWhenSelectedDayOnlyHasOriginSlots() {
        UUID transportId = UUID.randomUUID();
        Transport transport = buildTransport(transportId, "METRO-L2", TransportType.METRO);

        TransportStop originStop = buildStop(UUID.randomUUID(), "Origin");
        TransportStop midStop = buildStop(UUID.randomUUID(), "Mid");
        TransportStop terminusStop = buildStop(UUID.randomUUID(), "Terminus");

        when(transportRepository.findById(transportId)).thenReturn(Optional.of(transport));
        when(transportRouteStopRepository.findByTransportIdOrderByStopOrderAsc(transportId)).thenReturn(List.of(
            buildRouteStop(transport, originStop, 1),
            buildRouteStop(transport, midStop, 2),
            buildRouteStop(transport, terminusStop, 3)
        ));

        List<TransportDepartureSlot> sundaySlots = List.of(
            buildDepartureSlot(transport, originStop, "Sunday", LocalTime.of(8, 0), 1)
        );
        when(transportDepartureSlotRepository.findByTransportIdAndDayOfWeekIgnoreCaseOrderByStopOrderAscDepartureTimeAsc(
            transportId,
            "Sunday"
        )).thenReturn(sundaySlots);

        when(transportDepartureSlotRepository.findByTransportIdOrderByStopOrderAscDayOfWeekAscDepartureTimeAsc(transportId))
            .thenReturn(List.of(
                buildDepartureSlot(transport, originStop, "Monday", LocalTime.of(7, 0), 1),
                buildDepartureSlot(transport, terminusStop, "Monday", LocalTime.of(7, 12), 3),
                buildDepartureSlot(transport, originStop, "Sunday", LocalTime.of(8, 0), 1)
            ));

        ClientTransportDeparturesResponse response = clientService.getTransportDepartures(
            transportId,
            LocalDate.of(2026, 4, 19)
        );

        assertEquals(LocalTime.of(8, 0), response.getDepartures().get(0).getDepartureTime());
        assertEquals(LocalTime.of(8, 6), response.getDepartures().get(1).getDepartureTime());
        assertEquals(LocalTime.of(8, 12), response.getDepartures().get(2).getDepartureTime());
    }

    @Test
    void getTransportDeparturesPrefersExactSavedSlotsForCurrentDay() {
        UUID transportId = UUID.randomUUID();
        Transport transport = buildTransport(transportId, "METRO-L1", TransportType.METRO);

        TransportStop originStop = buildStop(UUID.randomUUID(), "Origin");
        TransportStop midStop = buildStop(UUID.randomUUID(), "Mid");
        TransportStop terminusStop = buildStop(UUID.randomUUID(), "Terminus");

        when(transportRepository.findById(transportId)).thenReturn(Optional.of(transport));
        when(transportRouteStopRepository.findByTransportIdOrderByStopOrderAsc(transportId)).thenReturn(List.of(
            buildRouteStop(transport, originStop, 1),
            buildRouteStop(transport, midStop, 2),
            buildRouteStop(transport, terminusStop, 3)
        ));

        List<TransportDepartureSlot> mondaySlots = List.of(
            buildDepartureSlot(transport, originStop, "Monday", LocalTime.of(7, 0), 1),
            buildDepartureSlot(transport, terminusStop, "Monday", LocalTime.of(7, 14), 3)
        );
        when(transportDepartureSlotRepository.findByTransportIdAndDayOfWeekIgnoreCaseOrderByStopOrderAscDepartureTimeAsc(
            transportId,
            "Monday"
        )).thenReturn(mondaySlots);

        when(transportDepartureSlotRepository.findByTransportIdOrderByStopOrderAscDayOfWeekAscDepartureTimeAsc(transportId))
            .thenReturn(List.of(
                buildDepartureSlot(transport, originStop, "Monday", LocalTime.of(7, 0), 1),
                buildDepartureSlot(transport, terminusStop, "Monday", LocalTime.of(7, 14), 3),
                buildDepartureSlot(transport, originStop, "Sunday", LocalTime.of(8, 0), 1),
                buildDepartureSlot(transport, terminusStop, "Sunday", LocalTime.of(8, 12), 3)
            ));

        ClientTransportDeparturesResponse response = clientService.getTransportDepartures(
            transportId,
            LocalDate.of(2026, 4, 20)
        );

        assertEquals(LocalTime.of(7, 0), response.getDepartures().get(0).getDepartureTime());
        assertEquals(LocalTime.of(7, 7), response.getDepartures().get(1).getDepartureTime());
        assertEquals(LocalTime.of(7, 14), response.getDepartures().get(2).getDepartureTime());
    }

    private User buildClientUser(UUID clientId, String email) {
        User user = new User();
        user.setId(clientId);
        user.setEmail(email);
        user.setRole(com.tuniway.connect.model.entity.Role.CLIENT);
        user.setStatus(AccountStatus.ACTIVE);
        return user;
    }

    private Transport buildTransport(UUID transportId, String code, TransportType type) {
        Transport transport = new Transport();
        transport.setId(transportId);
        transport.setCode(code);
        transport.setName(code);
        transport.setType(type);
        transport.setRoute_name(code);
        transport.setStart_point("Start");
        transport.setEnd_point("End");
        transport.setOperating_zone("Zone");
        transport.setZone("Zone");
        transport.setActive(true);
        return transport;
    }

    private TransportStop buildStop(UUID stopId, String name) {
        TransportStop stop = new TransportStop();
        stop.setId(stopId);
        stop.setStopName(name);
        stop.setZone("Zone");
        stop.setActive(true);
        return stop;
    }

    private TransportRouteStop buildRouteStop(Transport transport, TransportStop stop, int stopOrder) {
        TransportRouteStop routeStop = new TransportRouteStop();
        routeStop.setTransport(transport);
        routeStop.setStop(stop);
        routeStop.setStopOrder(stopOrder);
        routeStop.setActive(true);
        return routeStop;
    }

    private TransportDepartureSlot buildDepartureSlot(Transport transport,
                                                      TransportStop stop,
                                                      String dayOfWeek,
                                                      LocalTime departureTime,
                                                      int stopOrder) {
        TransportDepartureSlot slot = new TransportDepartureSlot();
        slot.setId(UUID.randomUUID());
        slot.setTransport(transport);
        slot.setStop(stop);
        slot.setDayOfWeek(dayOfWeek);
        slot.setDepartureTime(departureTime);
        slot.setStopOrder(stopOrder);
        slot.setActive(true);
        return slot;
    }
}
