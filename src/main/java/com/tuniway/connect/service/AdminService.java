package com.tuniway.connect.service;

import com.tuniway.connect.model.dto.AdminDashboardResponse;
import com.tuniway.connect.model.dto.AdminShiftResponse;
import com.tuniway.connect.model.dto.CreateTransportRequest;
import com.tuniway.connect.model.dto.PlanningPublishRequest;
import com.tuniway.connect.model.dto.PlanningPublishResponse;
import com.tuniway.connect.model.dto.ReassignTransportRequest;
import com.tuniway.connect.model.dto.RegisterEmployeeRequest;
import com.tuniway.connect.model.dto.RegisterEmployeeResponse;
import com.tuniway.connect.model.dto.TransportDepartureItem;
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
import com.tuniway.connect.model.entity.AccountStatus;
import com.tuniway.connect.model.entity.AdminProfile;
import com.tuniway.connect.model.entity.EmployeeProfile;
import com.tuniway.connect.model.entity.Role;
import com.tuniway.connect.model.entity.ShiftStopEvent;
import com.tuniway.connect.model.entity.Transport;
import com.tuniway.connect.model.entity.TransportDepartureSlot;
import com.tuniway.connect.model.entity.TransportRouteStop;
import com.tuniway.connect.model.entity.TransportStop;
import com.tuniway.connect.model.entity.User;
import com.tuniway.connect.model.entity.WorkShift;
import com.tuniway.connect.repository.AdminProfileRepository;
import com.tuniway.connect.repository.EmployeeProfileRepository;
import com.tuniway.connect.repository.ShiftStopEventRepository;
import com.tuniway.connect.repository.TransportDepartureSlotRepository;
import com.tuniway.connect.repository.TransportRepository;
import com.tuniway.connect.repository.TransportRouteStopRepository;
import com.tuniway.connect.repository.TransportStopRepository;
import com.tuniway.connect.repository.UserRepository;
import com.tuniway.connect.repository.WorkShiftRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminService {
    private static final EnumSet<Role> STAFF_ROLES = EnumSet.of(Role.ADMIN, Role.EMPLOYEE);
    private static final String SHIFT_STATUS_SCHEDULED = "SCHEDULED";

    private final UserRepository userRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final AdminProfileRepository adminProfileRepository;
    private final TransportRepository transportRepository;
    private final TransportStopRepository transportStopRepository;
    private final TransportRouteStopRepository transportRouteStopRepository;
    private final TransportDepartureSlotRepository transportDepartureSlotRepository;
    private final WorkShiftRepository workShiftRepository;
    private final ShiftStopEventRepository shiftStopEventRepository;

    public AdminService(
        UserRepository userRepository,
        EmployeeProfileRepository employeeProfileRepository,
        AdminProfileRepository adminProfileRepository,
        TransportRepository transportRepository,
        TransportStopRepository transportStopRepository,
        TransportRouteStopRepository transportRouteStopRepository,
        TransportDepartureSlotRepository transportDepartureSlotRepository,
        WorkShiftRepository workShiftRepository,
        ShiftStopEventRepository shiftStopEventRepository
    ) {
        this.userRepository = userRepository;
        this.employeeProfileRepository = employeeProfileRepository;
        this.adminProfileRepository = adminProfileRepository;
        this.transportRepository = transportRepository;
        this.transportStopRepository = transportStopRepository;
        this.transportRouteStopRepository = transportRouteStopRepository;
        this.transportDepartureSlotRepository = transportDepartureSlotRepository;
        this.workShiftRepository = workShiftRepository;
        this.shiftStopEventRepository = shiftStopEventRepository;
    }

    public AdminDashboardResponse getDashboard() {
        AdminDashboardResponse response = new AdminDashboardResponse();
        response.setSuccess(true);
        response.setMessage("Admin dashboard retrieved successfully");
        response.setTotalStaffAccounts(userRepository.countByRoleIn(STAFF_ROLES));
        response.setActiveStaffAccounts(userRepository.countByRoleInAndStatus(STAFF_ROLES, AccountStatus.ACTIVE));
        response.setTotalEmployeeAccounts(userRepository.countByRole(Role.EMPLOYEE));
        response.setTotalAdminAccounts(userRepository.countByRole(Role.ADMIN));
        response.setTotalTransports(transportRepository.count());
        response.setActiveTransports(transportRepository.countByActiveTrue());
        response.setTotalShifts(workShiftRepository.count());
        response.setScheduledShifts(workShiftRepository.countByStatusIgnoreCase(SHIFT_STATUS_SCHEDULED));
        response.setInProgressShifts(workShiftRepository.countByStatusIgnoreCase("IN_PROGRESS"));
        response.setCompletedShifts(workShiftRepository.countByStatusIgnoreCase("COMPLETED"));
        return response;
    }

    @Transactional
    public RegisterEmployeeResponse createStaffAccount(RegisterEmployeeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        Role role = requireStaffRole(request.getRole());
        String email = requireNonBlank(request.getEmail(), "Email is required");
        String password = requireNonBlank(request.getPassword_hash(), "Password is required");
        String fullName = requireNonBlank(request.getFullName(), "Full name is required");

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("A staff account with this email already exists");
        }

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setPassword_hash(password);
        user.setRole(role);
        user.setStatus(AccountStatus.ACTIVE);
        User savedUser = userRepository.save(user);

        RegisterEmployeeResponse response = new RegisterEmployeeResponse();
        response.setId(savedUser.getId());
        response.setEmail(savedUser.getEmail());
        response.setRole(savedUser.getRole());
        response.setStatus(savedUser.getStatus());
        response.setCreatedAt(savedUser.getCreatedAt());
        response.setFullName(fullName);

        if (role == Role.EMPLOYEE) {
            String employeeCode = requireNonBlank(request.getEmployee_code(), "Employee code is required for employee accounts");
            String licenseNumber = requireNonBlank(request.getLicense_number(), "License number is required for employee accounts");

            if (employeeProfileRepository.existsByEmployeeCode(employeeCode)) {
                throw new IllegalArgumentException("An employee with this employee code already exists");
            }
            if (employeeProfileRepository.existsByLicenseNumber(licenseNumber)) {
                throw new IllegalArgumentException("An employee with this license number already exists");
            }

            EmployeeProfile profile = new EmployeeProfile();
            profile.setUserId(savedUser.getId());
            profile.setFullName(fullName);
            profile.setPhone(normalizeToNull(request.getPhone()));
            profile.setLicenseNumber(licenseNumber);
            profile.setEmployeeCode(employeeCode);
            profile.setTwoFactorEnabled(Boolean.FALSE);
            EmployeeProfile savedProfile = employeeProfileRepository.save(profile);

            response.setPhone(savedProfile.getPhone());
            response.setLicense_number(savedProfile.getLicenseNumber());
            response.setEmployee_code(savedProfile.getEmployeeCode());
        } else {
            String adminCode = requireNonBlank(request.getAdmin_code(), "Admin code is required for admin accounts");
            if (adminProfileRepository.existsByAdminCode(adminCode)) {
                throw new IllegalArgumentException("An admin with this admin code already exists");
            }

            AdminProfile profile = new AdminProfile();
            profile.setUserId(savedUser.getId());
            profile.setFullName(fullName);
            profile.setAdminCode(adminCode);
            profile.setTwoFactorEnabled(Boolean.FALSE);
            AdminProfile savedProfile = adminProfileRepository.save(profile);

            response.setAdmin_code(savedProfile.getAdminCode());
        }

        response.setSuccess(true);
        response.setMessage("Staff account created successfully");
        return response;
    }

    @Transactional
    public UpdatedEmployeeResponse updateStaffAccount(UUID staffId, UpdatedEmployeeRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        User user = requireStaffUser(staffId);

        String email = normalizeToNull(request.getEmail());
        if (email != null && !email.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("A staff account with this email already exists");
        }
        if (email != null) {
            user.setEmail(email);
        }

        String password = normalizeToNull(request.getPassword_hash());
        if (password != null) {
            user.setPassword_hash(password);
        }

        if (user.getRole() == Role.EMPLOYEE) {
            EmployeeProfile profile = employeeProfileRepository.findByUserId(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Employee profile not found"));

            String fullName = normalizeToNull(request.getFullName());
            if (fullName != null) {
                profile.setFullName(fullName);
            }

            String phone = normalizeToNull(request.getPhone());
            if (phone != null) {
                profile.setPhone(phone);
            }

            String licenseNumber = normalizeToNull(request.getLicense_number());
            if (licenseNumber != null && !licenseNumber.equals(profile.getLicenseNumber())
                && employeeProfileRepository.existsByLicenseNumber(licenseNumber)) {
                throw new IllegalArgumentException("An employee with this license number already exists");
            }
            if (licenseNumber != null) {
                profile.setLicenseNumber(licenseNumber);
            }

            String employeeCode = normalizeToNull(request.getEmployee_code());
            if (employeeCode != null && !employeeCode.equals(profile.getEmployeeCode())
                && employeeProfileRepository.existsByEmployeeCode(employeeCode)) {
                throw new IllegalArgumentException("An employee with this employee code already exists");
            }
            if (employeeCode != null) {
                profile.setEmployeeCode(employeeCode);
            }

            employeeProfileRepository.save(profile);
        } else {
            AdminProfile profile = adminProfileRepository.findByUserId(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Admin profile not found"));

            String fullName = normalizeToNull(request.getFullName());
            if (fullName != null) {
                profile.setFullName(fullName);
            }

            String adminCode = normalizeToNull(request.getAdmin_code());
            if (adminCode != null && !adminCode.equals(profile.getAdminCode())
                && adminProfileRepository.existsByAdminCode(adminCode)) {
                throw new IllegalArgumentException("An admin with this admin code already exists");
            }
            if (adminCode != null) {
                profile.setAdminCode(adminCode);
            }

            adminProfileRepository.save(profile);
        }

        userRepository.save(user);

        UpdatedEmployeeResponse response = new UpdatedEmployeeResponse();
        response.setSuccess(true);
        response.setMessage("Staff account updated successfully");
        return response;
    }

    @Transactional
    public UpdatedEmployeeResponse changeStaffAccountStatus(UUID staffId, UpdatedEmployeeStatusRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        User user = requireStaffUser(staffId);
        String requestedStatus = requireNonBlank(request.getStatus(), "Status is required");

        try {
            AccountStatus newStatus = AccountStatus.valueOf(requestedStatus.trim().toUpperCase(Locale.ENGLISH));
            user.setStatus(newStatus);
            userRepository.save(user);

            UpdatedEmployeeResponse response = new UpdatedEmployeeResponse();
            response.setSuccess(true);
            response.setMessage("Staff account status updated successfully");
            return response;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value. Allowed values are: ACTIVE, INACTIVE");
        }
    }

    @Transactional
    public UpdatedEmployeeResponse deleteStaffAccount(UUID staffId) {
        User user = requireStaffUser(staffId);
        userRepository.delete(user);

        UpdatedEmployeeResponse response = new UpdatedEmployeeResponse();
        response.setSuccess(true);
        response.setMessage("Staff account deleted successfully");
        return response;
    }

    @Transactional
    public TransportResponse updateTransportRoute(UUID transportId, UpdateTransportRouteRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        Transport transport = requireTransport(transportId);
        transport.setRoute_name(requireNonBlank(request.getRoute(), "Route is required"));
        transportRepository.save(transport);
        return buildTransportResponse(transport, "Transport route updated successfully");
    }

    @Transactional
    public TransportResponse updateTransportZone(UUID transportId, UpdateTransportZoneRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        Transport transport = requireTransport(transportId);
        String zone = normalizeToNull(request.getZone());
        String operatingZone = normalizeToNull(request.getOperating_zone());

        if (zone == null && operatingZone == null) {
            throw new IllegalArgumentException("At least one zone field is required");
        }

        if (zone != null) {
            transport.setZone(zone);
        }
        if (operatingZone != null) {
            transport.setOperating_zone(operatingZone);
        }

        transportRepository.save(transport);
        return buildTransportResponse(transport, "Transport zone updated successfully");
    }

    @Transactional
    public TransportResponse updateTransportStops(UUID transportId, UpdateTransportStopsRequest request) {
        if (request == null || request.getStops() == null) {
            throw new IllegalArgumentException("Stops list is required");
        }

        Transport transport = requireTransport(transportId);
        replaceTransportStops(transport, request.getStops());
        syncShiftStopEventsForTransport(transportId);
        return buildTransportResponse(transport, "Transport stops updated successfully");
    }

    @Transactional
    public TransportResponse updateTransportDepartures(UUID transportId, UpdateTransportDeparturesRequest request) {
        if (request == null || request.getDepartures() == null) {
            throw new IllegalArgumentException("Departures list is required");
        }

        Transport transport = requireTransport(transportId);
        replaceTransportDepartures(transport, request.getDepartures());
        syncShiftStopEventsForTransport(transportId);
        return buildTransportResponse(transport, "Transport departures updated successfully");
    }

    @Transactional
    public TransportResponse updateTransport(UUID transportId, UpdateTransportRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        Transport transport = requireTransport(transportId);
        boolean shouldSyncShiftStops = false;

        String name = normalizeToNull(request.getName());
        if (name != null) {
            transport.setName(name);
        }

        if (request.getTransportType() != null) {
            transport.setType(request.getTransportType());
        }

        String code = normalizeToNull(request.getCode());
        if (code != null) {
            transportRepository.findByCode(code)
                .filter(existingTransport -> !existingTransport.getId().equals(transportId))
                .ifPresent(existingTransport -> {
                    throw new IllegalArgumentException("A transport with this code already exists");
                });
            transport.setCode(code);
        }

        String routeName = normalizeToNull(request.getRouteName());
        if (routeName != null) {
            transport.setRoute_name(routeName);
        }

        String startPoint = normalizeToNull(request.getStart_point());
        if (startPoint != null) {
            transport.setStart_point(startPoint);
        }

        String endPoint = normalizeToNull(request.getEnd_point());
        if (endPoint != null) {
            transport.setEnd_point(endPoint);
        }

        String operatingZone = normalizeToNull(request.getOperating_zone());
        if (operatingZone != null) {
            transport.setOperating_zone(operatingZone);
        }

        String zone = normalizeToNull(request.getZone());
        if (zone != null) {
            transport.setZone(zone);
        }

        if (request.getIs_active() != null) {
            transport.setActive(request.getIs_active());
        }

        transportRepository.save(transport);

        if (request.getStops() != null) {
            replaceTransportStops(transport, request.getStops());
            shouldSyncShiftStops = true;
        }

        if (request.getDepartures() != null) {
            replaceTransportDepartures(transport, request.getDepartures());
            shouldSyncShiftStops = true;
        }

        if (shouldSyncShiftStops) {
            syncShiftStopEventsForTransport(transportId);
        }

        return buildTransportResponse(transport, "Transport updated successfully");
    }

    @Transactional
    public TransportResponse createTransport(CreateTransportRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        String code = requireNonBlank(request.getCode(), "Transport code is required");
        String name = requireNonBlank(request.getName(), "Transport name is required");
        String routeName = requireNonBlank(request.getRouteName(), "Route name is required");
        String startPoint = requireNonBlank(request.getStart_point(), "Start point is required");
        String endPoint = requireNonBlank(request.getEnd_point(), "End point is required");
        String operatingZone = requireNonBlank(request.getOperating_zone(), "Operating zone is required");
        String zone = requireNonBlank(request.getZone(), "Zone is required");

        if (request.getTransportType() == null) {
            throw new IllegalArgumentException("Transport type is required");
        }
        if (transportRepository.findByCode(code).isPresent()) {
            throw new IllegalArgumentException("A transport with this code already exists");
        }

        Transport transport = new Transport();
        transport.setId(UUID.randomUUID());
        transport.setCode(code);
        transport.setName(name);
        transport.setType(request.getTransportType());
        transport.setRoute_name(routeName);
        transport.setStart_point(startPoint);
        transport.setEnd_point(endPoint);
        transport.setOperating_zone(operatingZone);
        transport.setZone(zone);
        transport.setActive(request.getIs_active() != null ? request.getIs_active() : Boolean.TRUE);
        Transport savedTransport = transportRepository.save(transport);

        if (request.getStops() != null) {
            replaceTransportStops(savedTransport, request.getStops());
        }
        if (request.getDepartures() != null) {
            replaceTransportDepartures(savedTransport, request.getDepartures());
        }

        return buildTransportResponse(savedTransport, "Transport created successfully");
    }

    @Transactional
    public AdminShiftResponse updateShift(UUID shiftId, UpdateShiftRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        return applyShiftUpdate(
            shiftId,
            request.getNewStart(),
            request.getNewEnd(),
            request.getTransportId(),
            "Shift updated successfully"
        );
    }

    @Transactional
    public AdminShiftResponse reassignEmployeeTransport(UUID shiftId, ReassignTransportRequest request) {
        if (request == null || request.getNewTransportId() == null) {
            throw new IllegalArgumentException("New transport id is required");
        }

        return applyShiftUpdate(
            shiftId,
            null,
            null,
            request.getNewTransportId(),
            "Shift transport reassigned successfully"
        );
    }

    @Transactional
    public PlanningPublishResponse publishPlanningChanges(PlanningPublishRequest request) {
        if (request == null || request.getChanges() == null || request.getChanges().isEmpty()) {
            throw new IllegalArgumentException("At least one planning change is required");
        }

        List<AdminShiftResponse> updatedShifts = new ArrayList<>();
        for (PlanningPublishRequest.PlanningChange change : request.getChanges()) {
            if (change == null || change.getShiftId() == null) {
                throw new IllegalArgumentException("Each planning change must include a shiftId");
            }

            updatedShifts.add(applyShiftUpdate(
                change.getShiftId(),
                change.getNewStart(),
                change.getNewEnd(),
                change.getTransportId(),
                "Shift updated successfully"
            ));
        }

        PlanningPublishResponse response = new PlanningPublishResponse();
        response.setSuccess(true);
        response.setMessage("Planning changes published successfully");
        response.setAppliedCount(updatedShifts.size());
        response.setShifts(updatedShifts);
        return response;
    }

    private AdminShiftResponse applyShiftUpdate(
        UUID shiftId,
        Instant newStart,
        Instant newEnd,
        UUID newTransportId,
        String successMessage
    ) {
        WorkShift shift = requireShift(shiftId);
        validateEditableShift(shift);

        Instant targetStart = newStart != null ? newStart : shift.getScheduleStart();
        Instant targetEnd = newEnd != null ? newEnd : shift.getScheduleEnd();
        validateShiftWindow(targetStart, targetEnd);

        Transport transport = newTransportId != null ? requireTransport(newTransportId) : shift.getTransport();

        boolean hasConflict = workShiftRepository.existsByEmployeeIdAndIdNotAndScheduleStartLessThanAndScheduleEndGreaterThan(
            shift.getEmployeeId(),
            shift.getId(),
            targetEnd,
            targetStart
        );
        if (hasConflict) {
            throw new IllegalArgumentException("Shift update would overlap with another shift for this employee");
        }

        shift.setScheduleStart(targetStart);
        shift.setScheduleEnd(targetEnd);
        shift.setTransport(transport);
        WorkShift savedShift = workShiftRepository.save(shift);
        regenerateShiftStopEvents(savedShift);
        return buildShiftResponse(savedShift, successMessage);
    }

    private void replaceTransportStops(Transport transport, List<TransportStopItem> stopItems) {
        transportRouteStopRepository.deleteByTransportId(transport.getId());

        if (stopItems.isEmpty()) {
            transportDepartureSlotRepository.deleteByTransportId(transport.getId());
            return;
        }

        List<TransportStopItem> orderedItems = new ArrayList<>(stopItems);
        orderedItems.sort(Comparator.comparing(TransportStopItem::getStopOrder, Comparator.nullsLast(Integer::compareTo)));

        Set<Integer> seenOrders = new HashSet<>();
        Set<UUID> seenStops = new HashSet<>();
        List<TransportRouteStop> routeStops = new ArrayList<>();

        for (TransportStopItem item : orderedItems) {
            if (item == null) {
                throw new IllegalArgumentException("Stops list contains an empty item");
            }
            if (item.getStopOrder() == null || item.getStopOrder() <= 0) {
                throw new IllegalArgumentException("Each stop must include a positive stopOrder");
            }
            if (!seenOrders.add(item.getStopOrder())) {
                throw new IllegalArgumentException("Duplicate stopOrder values are not allowed");
            }

            TransportStop stop = resolveTransportStop(item);
            if (!seenStops.add(stop.getId())) {
                throw new IllegalArgumentException("A stop can only appear once in the same transport route");
            }

            TransportRouteStop routeStop = new TransportRouteStop();
            routeStop.setTransport(transport);
            routeStop.setStop(stop);
            routeStop.setStopOrder(item.getStopOrder());
            routeStop.setActive(item.getActive() != null ? item.getActive() : Boolean.TRUE);
            routeStops.add(routeStop);
        }

        transportRouteStopRepository.saveAll(routeStops);
        syncDepartureSlotOrdersToRoute(transport.getId());
    }

    private void replaceTransportDepartures(Transport transport, List<TransportDepartureItem> departureItems) {
        List<TransportRouteStop> routeStops = transportRouteStopRepository.findByTransportIdOrderByStopOrderAsc(transport.getId());
        if (departureItems.isEmpty()) {
            transportDepartureSlotRepository.deleteByTransportId(transport.getId());
            return;
        }
        if (routeStops.isEmpty()) {
            throw new IllegalArgumentException("Transport stops must be configured before departures");
        }

        Map<UUID, TransportRouteStop> routeStopsByStopId = routeStops.stream()
            .collect(Collectors.toMap(routeStop -> routeStop.getStop().getId(), routeStop -> routeStop));

        transportDepartureSlotRepository.deleteByTransportId(transport.getId());

        Set<String> seenSlots = new HashSet<>();
        List<TransportDepartureSlot> slots = new ArrayList<>();
        for (TransportDepartureItem item : departureItems) {
            if (item == null) {
                throw new IllegalArgumentException("Departures list contains an empty item");
            }
            if (item.getStopId() == null) {
                throw new IllegalArgumentException("Each departure must include a stopId");
            }

            TransportRouteStop routeStop = routeStopsByStopId.get(item.getStopId());
            if (routeStop == null) {
                throw new IllegalArgumentException("Departure stop does not belong to this transport route");
            }
            if (item.getDepartureTime() == null) {
                throw new IllegalArgumentException("Each departure must include a departureTime");
            }

            Integer routeOrder = routeStop.getStopOrder();
            if (item.getStopOrder() != null && !Objects.equals(item.getStopOrder(), routeOrder)) {
                throw new IllegalArgumentException("Departure stopOrder must match the configured transport stop order");
            }

            String normalizedDayOfWeek = normalizeDayOfWeek(item.getDayOfWeek());
            String slotKey = item.getStopId() + "|" + normalizedDayOfWeek + "|" + item.getDepartureTime();
            if (!seenSlots.add(slotKey)) {
                throw new IllegalArgumentException("Duplicate departure entries are not allowed");
            }

            TransportDepartureSlot slot = new TransportDepartureSlot();
            slot.setTransport(transport);
            slot.setStop(routeStop.getStop());
            slot.setStopOrder(routeOrder);
            slot.setDayOfWeek(normalizedDayOfWeek);
            slot.setDepartureTime(item.getDepartureTime());
            slot.setActive(item.getActive() != null ? item.getActive() : Boolean.TRUE);
            slots.add(slot);
        }

        transportDepartureSlotRepository.saveAll(slots);
    }

    private TransportStop resolveTransportStop(TransportStopItem item) {
        if (item.getStopId() != null) {
            TransportStop existingStop = transportStopRepository.findById(item.getStopId())
                .orElseThrow(() -> new IllegalArgumentException("Transport stop not found: " + item.getStopId()));

            String stopName = normalizeToNull(item.getStopName());
            if (stopName != null) {
                existingStop.setStopName(stopName);
            }

            String zone = normalizeToNull(item.getZone());
            if (zone != null) {
                existingStop.setZone(zone);
            }

            if (item.getActive() != null) {
                existingStop.setActive(item.getActive());
            }

            if (item.getLatitude() != null) {
                validateLatitude(item.getLatitude());
                existingStop.setLatitude(item.getLatitude());
            }

            if (item.getLongitude() != null) {
                validateLongitude(item.getLongitude());
                existingStop.setLongitude(item.getLongitude());
            }

            return transportStopRepository.save(existingStop);
        }

        String stopName = requireNonBlank(item.getStopName(), "Each new stop must include a stopName");
        TransportStop newStop = new TransportStop();
        newStop.setStopName(stopName);
        newStop.setZone(normalizeToNull(item.getZone()));
        newStop.setActive(item.getActive() != null ? item.getActive() : Boolean.TRUE);
        validateLatitude(item.getLatitude());
        newStop.setLatitude(item.getLatitude());
        validateLongitude(item.getLongitude());
        newStop.setLongitude(item.getLongitude());
        return transportStopRepository.save(newStop);
    }

    private void validateLatitude(BigDecimal latitude) {
        if (latitude == null) {
            return;
        }
        if (latitude.compareTo(BigDecimal.valueOf(-90)) < 0 || latitude.compareTo(BigDecimal.valueOf(90)) > 0) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
    }

    private void validateLongitude(BigDecimal longitude) {
        if (longitude == null) {
            return;
        }
        if (longitude.compareTo(BigDecimal.valueOf(-180)) < 0 || longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
    }

    private void syncDepartureSlotOrdersToRoute(UUID transportId) {
        Map<UUID, Integer> routeOrders = transportRouteStopRepository.findByTransportIdOrderByStopOrderAsc(transportId).stream()
            .collect(Collectors.toMap(routeStop -> routeStop.getStop().getId(), TransportRouteStop::getStopOrder));

        if (routeOrders.isEmpty()) {
            transportDepartureSlotRepository.deleteByTransportId(transportId);
            return;
        }

        List<TransportDepartureSlot> slots = transportDepartureSlotRepository
            .findByTransportIdOrderByStopOrderAscDayOfWeekAscDepartureTimeAsc(transportId);
        List<TransportDepartureSlot> slotsToKeep = new ArrayList<>();
        List<TransportDepartureSlot> slotsToDelete = new ArrayList<>();

        for (TransportDepartureSlot slot : slots) {
            Integer routeOrder = routeOrders.get(slot.getStop().getId());
            if (routeOrder == null) {
                slotsToDelete.add(slot);
                continue;
            }
            slot.setStopOrder(routeOrder);
            slotsToKeep.add(slot);
        }

        if (!slotsToDelete.isEmpty()) {
            transportDepartureSlotRepository.deleteAll(slotsToDelete);
        }
        if (!slotsToKeep.isEmpty()) {
            transportDepartureSlotRepository.saveAll(slotsToKeep);
        }
    }

    private void syncShiftStopEventsForTransport(UUID transportId) {
        List<WorkShift> shifts = workShiftRepository.findByTransportId(transportId);
        for (WorkShift shift : shifts) {
            if (isEditableShift(shift)) {
                regenerateShiftStopEvents(shift);
            }
        }
    }

    private void regenerateShiftStopEvents(WorkShift shift) {
        shiftStopEventRepository.deleteByWorkShiftId(shift.getId());

        List<TransportRouteStop> routeStops = transportRouteStopRepository.findByTransportIdOrderByStopOrderAsc(
            shift.getTransport().getId()
        ).stream()
            .filter(routeStop -> !Boolean.FALSE.equals(routeStop.getActive()))
            .collect(Collectors.toList());

        if (routeStops.isEmpty()) {
            return;
        }

        Map<UUID, Instant> expectedTimes = calculateExpectedStopTimes(shift, routeStops);
        Instant now = Instant.now();
        List<ShiftStopEvent> events = new ArrayList<>();

        for (TransportRouteStop routeStop : routeStops) {
            ShiftStopEvent event = new ShiftStopEvent();
            event.setWorkShift(shift);
            event.setStop(routeStop.getStop());
            event.setStopOrder(routeStop.getStopOrder());
            event.setExpectedDepartureTime(expectedTimes.get(routeStop.getStop().getId()));
            event.setStatus("pending");
            event.setCreatedAt(now);
            event.setUpdatedAt(now);
            events.add(event);
        }

        shiftStopEventRepository.saveAll(events);
    }

    private Map<UUID, Instant> calculateExpectedStopTimes(WorkShift shift, List<TransportRouteStop> routeStops) {
        Map<UUID, Instant> slotBasedTimes = findDepartureSlotBasedStopTimes(shift, routeStops);
        if (slotBasedTimes.size() == routeStops.size()) {
            return slotBasedTimes;
        }
        return buildEvenlyDistributedStopTimes(shift, routeStops);
    }

    private Map<UUID, Instant> findDepartureSlotBasedStopTimes(WorkShift shift, List<TransportRouteStop> routeStops) {
        List<TransportDepartureSlot> daySlots = transportDepartureSlotRepository
            .findByTransportIdOrderByStopOrderAscDayOfWeekAscDepartureTimeAsc(shift.getTransport().getId())
            .stream()
            .filter(slot -> !Boolean.FALSE.equals(slot.getActive()))
            .filter(slot -> normalizeDayOfWeek(slot.getDayOfWeek()).equals(
                normalizeDayOfWeek(shift.getScheduleStart().atZone(ZoneOffset.UTC).getDayOfWeek().name())
            ))
            .collect(Collectors.toList());

        if (daySlots.isEmpty()) {
            return new LinkedHashMap<>();
        }

        Map<UUID, List<TransportDepartureSlot>> slotsByStopId = daySlots.stream()
            .collect(Collectors.groupingBy(slot -> slot.getStop().getId(), LinkedHashMap::new, Collectors.toList()));

        LocalDate startDate = shift.getScheduleStart().atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate endDate = shift.getScheduleEnd().atZone(ZoneOffset.UTC).toLocalDate();
        Instant previousTime = null;
        Map<UUID, Instant> expectedTimes = new LinkedHashMap<>();

        for (TransportRouteStop routeStop : routeStops) {
            List<TransportDepartureSlot> candidates = slotsByStopId.get(routeStop.getStop().getId());
            if (candidates == null || candidates.isEmpty()) {
                return new LinkedHashMap<>();
            }

            Instant matchedTime = null;
            for (TransportDepartureSlot candidate : candidates) {
                List<Instant> possibleInstants = buildCandidateDepartureInstants(
                    startDate,
                    endDate,
                    candidate.getDepartureTime()
                );

                for (Instant candidateInstant : possibleInstants) {
                    if (candidateInstant.isBefore(shift.getScheduleStart()) || candidateInstant.isAfter(shift.getScheduleEnd())) {
                        continue;
                    }
                    if (previousTime != null && candidateInstant.isBefore(previousTime)) {
                        continue;
                    }
                    matchedTime = candidateInstant;
                    break;
                }

                if (matchedTime != null) {
                    break;
                }
            }

            if (matchedTime == null) {
                return new LinkedHashMap<>();
            }

            expectedTimes.put(routeStop.getStop().getId(), matchedTime);
            previousTime = matchedTime;
        }

        return expectedTimes;
    }

    private List<Instant> buildCandidateDepartureInstants(LocalDate startDate, LocalDate endDate, java.time.LocalTime departureTime) {
        List<Instant> candidates = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            candidates.add(LocalDateTime.of(currentDate, departureTime).toInstant(ZoneOffset.UTC));
            currentDate = currentDate.plusDays(1);
        }
        return candidates;
    }

    private Map<UUID, Instant> buildEvenlyDistributedStopTimes(WorkShift shift, List<TransportRouteStop> routeStops) {
        Map<UUID, Instant> expectedTimes = new LinkedHashMap<>();
        Duration totalDuration = Duration.between(shift.getScheduleStart(), shift.getScheduleEnd());

        if (totalDuration.isNegative() || totalDuration.isZero()) {
            for (TransportRouteStop routeStop : routeStops) {
                expectedTimes.put(routeStop.getStop().getId(), shift.getScheduleStart());
            }
            return expectedTimes;
        }

        long divisor = routeStops.size() + 1L;
        Duration step = totalDuration.dividedBy(divisor);

        for (int index = 0; index < routeStops.size(); index++) {
            TransportRouteStop routeStop = routeStops.get(index);
            Instant expectedTime = shift.getScheduleStart().plus(step.multipliedBy(index + 1L));
            expectedTimes.put(routeStop.getStop().getId(), expectedTime);
        }

        return expectedTimes;
    }

    private void validateEditableShift(WorkShift shift) {
        if (!isEditableShift(shift)) {
            throw new IllegalArgumentException("Only scheduled shifts can be updated or reassigned");
        }
    }

    private boolean isEditableShift(WorkShift shift) {
        return SHIFT_STATUS_SCHEDULED.equals(normalizeShiftStatus(shift.getStatus()));
    }

    private void validateShiftWindow(Instant start, Instant end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Both newStart and newEnd must be resolvable");
        }
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Shift start time must be before end time");
        }
    }

    private String normalizeShiftStatus(String status) {
        return status == null ? SHIFT_STATUS_SCHEDULED : status.trim().toUpperCase(Locale.ENGLISH);
    }

    private String normalizeDayOfWeek(String dayOfWeek) {
        String value = requireNonBlank(dayOfWeek, "Day of week is required");
        DayOfWeek normalized = DayOfWeek.valueOf(value.trim().toUpperCase(Locale.ENGLISH));
        return normalized.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

    private User requireStaffUser(UUID staffId) {
        User user = userRepository.findById(staffId)
            .orElseThrow(() -> new IllegalArgumentException("Staff account not found"));
        if (!STAFF_ROLES.contains(user.getRole())) {
            throw new IllegalArgumentException("The requested user is not a staff account");
        }
        return user;
    }

    private Role requireStaffRole(Role role) {
        Role resolvedRole = role != null ? role : Role.EMPLOYEE;
        if (!STAFF_ROLES.contains(resolvedRole)) {
            throw new IllegalArgumentException("Only ADMIN or EMPLOYEE staff accounts are supported");
        }
        return resolvedRole;
    }

    private WorkShift requireShift(UUID shiftId) {
        return workShiftRepository.findById(shiftId)
            .orElseThrow(() -> new IllegalArgumentException("Shift not found"));
    }

    private Transport requireTransport(UUID transportId) {
        return transportRepository.findById(transportId)
            .orElseThrow(() -> new IllegalArgumentException("Transport not found"));
    }

    private TransportResponse buildTransportResponse(Transport transport, String message) {
        return new TransportResponse(transport.getCode(), transport.getType(), message, true);
    }

    private AdminShiftResponse buildShiftResponse(WorkShift shift, String message) {
        AdminShiftResponse response = new AdminShiftResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setShiftId(shift.getId());
        response.setEmployeeId(shift.getEmployeeId());
        response.setTransportId(shift.getTransport() != null ? shift.getTransport().getId() : null);
        response.setTransportName(shift.getTransport() != null ? shift.getTransport().getName() : null);
        response.setScheduleStart(shift.getScheduleStart());
        response.setScheduleEnd(shift.getScheduleEnd());
        response.setStatus(shift.getStatus());
        response.setActualStart(shift.getActualStart());
        response.setActualEnd(shift.getActualEnd());
        return response;
    }

    private String requireNonBlank(String value, String message) {
        String normalized = normalizeToNull(value);
        if (normalized == null) {
            throw new IllegalArgumentException(message);
        }
        return normalized;
    }

    private String normalizeToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
