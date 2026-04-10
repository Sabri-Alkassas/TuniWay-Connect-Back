package com.tuniway.connect.service;

import com.tuniway.connect.model.dto.ClientAccountResponse;
import com.tuniway.connect.model.dto.ClientDashboardResponse;
import com.tuniway.connect.model.dto.ClientNearbyTransportsResponse;
import com.tuniway.connect.model.dto.ClientTicketDto;
import com.tuniway.connect.model.dto.ClientTicketHistoryResponse;
import com.tuniway.connect.model.dto.ClientTicketPaymentDto;
import com.tuniway.connect.model.dto.ClientTicketProductDto;
import com.tuniway.connect.model.dto.ClientTicketProductsResponse;
import com.tuniway.connect.model.dto.ClientTicketPurchaseResponse;
import com.tuniway.connect.model.dto.ClientTransportDepartureDto;
import com.tuniway.connect.model.dto.ClientTransportDeparturesResponse;
import com.tuniway.connect.model.dto.ClientTransportDetailsResponse;
import com.tuniway.connect.model.dto.ClientTransportDto;
import com.tuniway.connect.model.dto.ClientTransportSearchResponse;
import com.tuniway.connect.model.dto.ClientTransportStopDto;
import com.tuniway.connect.model.dto.ClientTransportStopsResponse;
import com.tuniway.connect.model.dto.PurchaseClientTicketRequest;
import com.tuniway.connect.model.dto.UpdateClientAccountRequest;
import com.tuniway.connect.model.entity.AccountStatus;
import com.tuniway.connect.model.entity.ClientProfile;
import com.tuniway.connect.model.entity.EmailVerificationCode;
import com.tuniway.connect.model.entity.PaymentTransaction;
import com.tuniway.connect.model.entity.Role;
import com.tuniway.connect.model.entity.TicketProduct;
import com.tuniway.connect.model.entity.TicketPurchase;
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
import com.tuniway.connect.repository.TicketProductRepository;
import com.tuniway.connect.repository.TicketPurchaseRepository;
import com.tuniway.connect.repository.TransportCountProjection;
import com.tuniway.connect.repository.TransportDepartureSlotRepository;
import com.tuniway.connect.repository.TransportRepository;
import com.tuniway.connect.repository.TransportRouteStopRepository;
import com.tuniway.connect.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ClientService {
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_RADIUS_METERS = 500;
    private static final int MAX_DEPARTURE_DELAY_TOLERANCE_MINUTES = 5;
    private static final String DEFAULT_SORT = "name,asc";
    private static final String WORK_SHIFT_STATUS_IN_PROGRESS = "IN_PROGRESS";
    private static final String STOP_EVENT_STATUS_DEPARTED = "departed";
    private static final String TICKET_STATUS_ACTIVE = "ACTIVE";
    private static final String PAYMENT_STATUS_COMPLETED = "COMPLETED";
    private static final double EARTH_RADIUS_METERS = 6_371_000d;

    private final UserRepository userRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final EmailVerificationCodeRepository emailVerificationCodeRepository;
    private final EmailService emailService;
    private final TransportRepository transportRepository;
    private final TransportRouteStopRepository transportRouteStopRepository;
    private final TransportDepartureSlotRepository transportDepartureSlotRepository;
    private final TicketProductRepository ticketProductRepository;
    private final TicketPurchaseRepository ticketPurchaseRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final ShiftStopEventRepository shiftStopEventRepository;

    public ClientService(
        UserRepository userRepository,
        ClientProfileRepository clientProfileRepository,
        EmailVerificationCodeRepository emailVerificationCodeRepository,
        EmailService emailService,
        TransportRepository transportRepository,
        TransportRouteStopRepository transportRouteStopRepository,
        TransportDepartureSlotRepository transportDepartureSlotRepository,
        TicketProductRepository ticketProductRepository,
        TicketPurchaseRepository ticketPurchaseRepository,
        PaymentTransactionRepository paymentTransactionRepository,
        ShiftStopEventRepository shiftStopEventRepository
    ) {
        this.userRepository = userRepository;
        this.clientProfileRepository = clientProfileRepository;
        this.emailVerificationCodeRepository = emailVerificationCodeRepository;
        this.emailService = emailService;
        this.transportRepository = transportRepository;
        this.transportRouteStopRepository = transportRouteStopRepository;
        this.transportDepartureSlotRepository = transportDepartureSlotRepository;
        this.ticketProductRepository = ticketProductRepository;
        this.ticketPurchaseRepository = ticketPurchaseRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.shiftStopEventRepository = shiftStopEventRepository;
    }

    public ClientDashboardResponse getDashboard(UUID clientId) {
        User user = requireClientUser(clientId);
        ClientProfile profile = requireClientProfile(clientId);
        List<String> missingProfileFields = collectMissingProfileFields(profile);

        ClientDashboardResponse response = new ClientDashboardResponse();
        response.setSuccess(true);
        response.setMessage("Client dashboard retrieved successfully");
        response.setClientId(user.getId());
        response.setEmail(user.getEmail());
        response.setUsername(profile.getUsername());
        response.setDisplayName(buildDisplayName(profile));
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());
        response.setLastLoginAt(user.getLastLoginAt());
        response.setEmailVerified(user.getStatus() == AccountStatus.ACTIVE);
        response.setProfileComplete(missingProfileFields.isEmpty());
        response.setMissingProfileFields(missingProfileFields);
        return response;
    }

    public ClientAccountResponse getAccount(UUID clientId) {
        User user = requireClientUser(clientId);
        ClientProfile profile = requireClientProfile(clientId);
        return buildAccountResponse(user, profile, "Client account retrieved successfully");
    }

    @Transactional
    public ClientAccountResponse updateAccount(UUID clientId, UpdateClientAccountRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        User user = requireClientUser(clientId);
        ClientProfile profile = requireClientProfile(clientId);
        boolean hasChanges = false;

        String email = normalizeToNull(request.getEmail());
        if (email != null) {
            if (!email.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("A client account with this email already exists");
            }
            if (!email.equalsIgnoreCase(user.getEmail())) {
                user.setEmail(email);
                user.setStatus(AccountStatus.INACTIVE);
                issueEmailVerificationCode(user);
                hasChanges = true;
            }
        }

        String password = normalizeToNull(request.getPassword_hash());
        if (password != null) {
            throw new IllegalArgumentException(
                "Password changes are not supported through account update. Use a dedicated password change flow."
            );
        }

        String username = normalizeToNull(request.getUsername());
        if (username != null) {
            clientProfileRepository.findByUsername(username)
                .filter(existingProfile -> !existingProfile.getUserId().equals(clientId))
                .ifPresent(existingProfile -> {
                    throw new IllegalArgumentException("A client with this username already exists");
                });
            profile.setUsername(username);
            hasChanges = true;
        }

        String firstName = normalizeToNull(request.getFirstName());
        if (firstName != null) {
            profile.setFirstName(firstName);
            hasChanges = true;
        }

        String lastName = normalizeToNull(request.getLastName());
        if (lastName != null) {
            profile.setLastName(lastName);
            hasChanges = true;
        }

        String phone = normalizeToNull(request.getPhone());
        if (phone != null) {
            profile.setPhone(phone);
            hasChanges = true;
        }

        if (request.getBirthDate() != null) {
            profile.setBirthDate(request.getBirthDate());
            hasChanges = true;
        }

        if (!hasChanges) {
            throw new IllegalArgumentException("At least one field is required");
        }

        userRepository.save(user);
        clientProfileRepository.save(profile);
        return buildAccountResponse(user, profile, "Client account updated successfully");
    }

    public ClientTransportSearchResponse searchTransports(String q,
                                                         String zone,
                                                         String type,
                                                         Boolean active,
                                                         int page,
                                                         int size,
                                                         String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        TransportType transportType = parseTransportType(type);

        Page<Transport> transports = transportRepository.searchForClient(
            normalizeToLowerCase(q),
            normalizeToLowerCase(zone),
            transportType,
            active,
            pageable
        );

        List<UUID> transportIds = transports.getContent().stream().map(Transport::getId).toList();
        Map<UUID, Long> stopCounts = loadVisibleStopCounts(transportIds);
        Map<UUID, Long> departureCounts = loadVisibleDepartureCounts(transportIds);

        ClientTransportSearchResponse response = new ClientTransportSearchResponse();
        response.setSuccess(true);
        response.setMessage("Client transports retrieved successfully");
        response.setTransports(transports.getContent().stream()
            .map(transport -> toTransportDto(transport, false, stopCounts, departureCounts))
            .toList());
        response.setPage(transports.getNumber());
        response.setSize(transports.getSize());
        response.setTotalItems(transports.getTotalElements());
        response.setTotalPages(transports.getTotalPages());
        response.setSort(normalizeSortExpression(sort));
        return response;
    }

    public ClientTransportDetailsResponse getTransport(UUID transportId) {
        Transport transport = requireTransport(transportId);

        ClientTransportDetailsResponse response = new ClientTransportDetailsResponse();
        response.setSuccess(true);
        response.setMessage("Transport retrieved successfully");
        response.setTransport(toTransportDto(transport, true));
        return response;
    }

    public ClientTransportStopsResponse getTransportStops(UUID transportId) {
        Transport transport = requireTransport(transportId);

        List<ClientTransportStopDto> stops = transportRouteStopRepository.findByTransportIdOrderByStopOrderAsc(transportId).stream()
            .filter(this::isClientVisibleRouteStop)
            .map(this::toTransportStopDto)
            .toList();

        ClientTransportStopsResponse response = new ClientTransportStopsResponse();
        response.setSuccess(true);
        response.setMessage("Transport stops retrieved successfully");
        response.setTransportId(transport.getId());
        response.setTransportName(transport.getName());
        response.setStops(stops);
        return response;
    }

    public ClientTransportDeparturesResponse getTransportDepartures(UUID transportId, LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("date is required");
        }

        Transport transport = requireTransport(transportId);
        String dayOfWeek = normalizeDisplayDayOfWeek(date.getDayOfWeek().name());

        List<ClientTransportDepartureDto> departures = transportDepartureSlotRepository
            .findByTransportIdAndDayOfWeekIgnoreCaseOrderByStopOrderAscDepartureTimeAsc(transportId, dayOfWeek)
            .stream()
            .filter(this::isClientVisibleDeparture)
            .map(this::toTransportDepartureDto)
            .toList();

        ClientTransportDeparturesResponse response = new ClientTransportDeparturesResponse();
        response.setSuccess(true);
        response.setMessage("Transport departures retrieved successfully");
        response.setTransportId(transport.getId());
        response.setTransportName(transport.getName());
        response.setDate(date);
        response.setDayOfWeek(dayOfWeek);
        response.setDepartures(departures);
        return response;
    }

    public ClientTicketProductsResponse getTicketProducts(UUID clientId,
                                                          UUID transportId,
                                                          UUID fromStopId,
                                                          UUID toStopId) {
        requireClientUser(clientId);

        RouteContext routeContext = tryResolveRouteContextForProducts(transportId, fromStopId, toStopId);
        if (routeContext == null) {
            ClientTicketProductsResponse response = new ClientTicketProductsResponse();
            response.setSuccess(true);
            response.setMessage("No eligible ticket products for the selected route segment");
            response.setProducts(List.of());
            return response;
        }

        List<ClientTicketProductDto> products = ticketProductRepository
            .findByActiveTrueOrderByPriceAscValidDurationMinutesAscNameAsc()
            .stream()
            .map(product -> toTicketProductDto(product, routeContext))
            .toList();

        if (products.isEmpty()) {
            throw new IllegalArgumentException("No active ticket products available");
        }

        ClientTicketProductsResponse response = new ClientTicketProductsResponse();
        response.setSuccess(true);
        response.setMessage("Ticket products retrieved successfully");
        response.setProducts(products);
        return response;
    }

    private RouteContext tryResolveRouteContextForProducts(UUID transportId, UUID fromStopId, UUID toStopId) {
        try {
            RouteContext routeContext = resolveRouteContext(transportId, fromStopId, toStopId);
            validateDepartureEligibility(routeContext, null);
            return routeContext;
        } catch (TicketEligibilityException ex) {
            return null;
        }
    }

    @Transactional
    public ClientTicketPurchaseResponse purchaseTicket(UUID clientId, PurchaseClientTicketRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        requireClientUser(clientId);

        RouteContext routeContext = resolveRouteContext(
            request.getTransportId(),
            request.getFromStopId(),
            request.getToStopId()
        );

        Transport lockedTransport = transportRepository.findByIdForUpdate(routeContext.transport().getId())
            .orElseThrow(() -> new IllegalArgumentException("Transport not found"));
        if (Boolean.FALSE.equals(lockedTransport.getActive())) {
            throw new IllegalArgumentException("Transport is inactive");
        }

        RouteContext lockedRouteContext = new RouteContext(
            lockedTransport,
            routeContext.fromStop(),
            routeContext.toStop(),
            routeContext.fromStopOrder(),
            routeContext.toStopOrder(),
            routeContext.stopCount()
        );

        validateDepartureEligibility(lockedRouteContext, request.getPlannedDepartureTime());

        TicketProduct product = resolveTicketProduct(request.getProductId());
        Instant purchaseTime = Instant.now();

        TicketPurchase purchase = new TicketPurchase();
        purchase.setUserId(clientId);
        purchase.setProduct(product);
        purchase.setTransport(lockedRouteContext.transport());
        purchase.setFromStop(lockedRouteContext.fromStop());
        purchase.setToStop(lockedRouteContext.toStop());
        purchase.setStopCount(lockedRouteContext.stopCount());
        purchase.setStatus(TICKET_STATUS_ACTIVE);
        purchase.setPurchaseTime(purchaseTime);
        purchase.setValidUntil(purchaseTime.plus(product.getValidDurationMinutes(), ChronoUnit.MINUTES));
        TicketPurchase savedPurchase = ticketPurchaseRepository.save(purchase);

        PaymentTransaction payment = new PaymentTransaction();
        payment.setTicketPurchase(savedPurchase);
        payment.setProvider(resolveRequiredOrDefault(request.getProvider(), "MANUAL"));
        payment.setProviderReference(resolveRequiredOrDefault(request.getProviderReference(), "MANUAL-" + savedPurchase.getId()));
        payment.setPaymentMethod(resolveRequiredOrDefault(request.getPaymentMethod(), "CASH"));
        payment.setProcessedAt(purchaseTime);
        payment.setAmount(calculateRouteFare(lockedRouteContext.transport().getType(), lockedRouteContext.stopCount()));
        payment.setStatus(PAYMENT_STATUS_COMPLETED);
        PaymentTransaction savedPayment = paymentTransactionRepository.save(payment);

        ClientTicketPurchaseResponse response = new ClientTicketPurchaseResponse();
        response.setSuccess(true);
        response.setMessage("Ticket purchased successfully");
        response.setTicket(toTicketDto(savedPurchase, savedPayment));
        return response;
    }

    public ClientTicketHistoryResponse getTicketHistory(UUID clientId, int page, int size, String sort) {
        requireClientUser(clientId);

        Pageable pageable = buildTicketPageable(page, size, sort);
        Page<TicketPurchase> purchases = ticketPurchaseRepository.findByUserId(clientId, pageable);

        List<UUID> purchaseIds = purchases.getContent().stream().map(TicketPurchase::getId).toList();
        Map<UUID, PaymentTransaction> latestPayments;
        if (purchaseIds.isEmpty()) {
            latestPayments = Map.of();
        } else {
            latestPayments = paymentTransactionRepository
                .findByTicketPurchaseIdInOrderByProcessedAtDesc(purchaseIds)
                .stream()
                .collect(Collectors.toMap(
                    payment -> payment.getTicketPurchase().getId(),
                    payment -> payment,
                    (left, right) -> left
                ));
        }

        ClientTicketHistoryResponse response = new ClientTicketHistoryResponse();
        response.setSuccess(true);
        response.setMessage("Ticket history retrieved successfully");
        response.setTickets(purchases.getContent().stream()
            .map(purchase -> toTicketDto(purchase, latestPayments.get(purchase.getId())))
            .toList());
        response.setPage(purchases.getNumber());
        response.setSize(purchases.getSize());
        response.setTotalItems(purchases.getTotalElements());
        response.setTotalPages(purchases.getTotalPages());
        response.setSort(normalizeTicketSortExpression(sort));
        return response;
    }

    public ClientNearbyTransportsResponse getNearbyTransports(double latitude, double longitude, Integer radiusMeters) {
        validateCoordinates(latitude, longitude);

        int resolvedRadiusMeters = radiusMeters != null ? radiusMeters : DEFAULT_RADIUS_METERS;
        if (resolvedRadiusMeters <= 0) {
            throw new IllegalArgumentException("radiusMeters must be greater than 0");
        }

        BoundingBox boundingBox = buildBoundingBox(latitude, longitude, resolvedRadiusMeters);
        Map<UUID, NearbyCandidate> nearbyCandidates = new HashMap<>();

        for (TransportRouteStop routeStop : transportRouteStopRepository.findEligibleNearbyRouteStops(
            boundingBox.minLatitude(),
            boundingBox.maxLatitude(),
            boundingBox.minLongitude(),
            boundingBox.maxLongitude()
        )) {
            if (!isEligibleNearbyRouteStop(routeStop)) {
                continue;
            }

            TransportStop stop = routeStop.getStop();
            double distanceMeters = calculateDistanceMeters(
                latitude,
                longitude,
                stop.getLatitude().doubleValue(),
                stop.getLongitude().doubleValue()
            );

            if (distanceMeters > resolvedRadiusMeters) {
                continue;
            }

            UUID transportId = routeStop.getTransport().getId();
            NearbyCandidate existingCandidate = nearbyCandidates.get(transportId);

            if (existingCandidate == null) {
                nearbyCandidates.put(transportId, new NearbyCandidate(routeStop.getTransport(), routeStop, distanceMeters, 1));
                continue;
            }

            existingCandidate.incrementMatchCount();
            if (distanceMeters < existingCandidate.distanceMeters()) {
                existingCandidate.setNearestStop(routeStop);
                existingCandidate.setDistanceMeters(distanceMeters);
            }
        }

        Map<UUID, Long> stopCounts = loadVisibleStopCounts(nearbyCandidates.keySet());
        Map<UUID, Long> departureCounts = loadVisibleDepartureCounts(nearbyCandidates.keySet());

        List<ClientNearbyTransportsResponse.NearbyTransportDto> transports = nearbyCandidates.values().stream()
            .sorted(Comparator
                .comparingDouble(NearbyCandidate::distanceMeters)
                .thenComparing(candidate -> candidate.transport().getName(), String.CASE_INSENSITIVE_ORDER))
            .map(candidate -> {
                ClientNearbyTransportsResponse.NearbyTransportDto dto =
                    new ClientNearbyTransportsResponse.NearbyTransportDto();
                dto.setTransport(toTransportDto(candidate.transport(), false, stopCounts, departureCounts));
                dto.setNearestStop(toTransportStopDto(candidate.nearestStop()));
                dto.setDistanceMeters(roundDistance(candidate.distanceMeters()));
                dto.setMatchingStopCount(candidate.matchCount());
                return dto;
            })
            .toList();

        ClientNearbyTransportsResponse response = new ClientNearbyTransportsResponse();
        response.setSuccess(true);
        response.setMessage("Nearby transports retrieved successfully");
        response.setLatitude(latitude);
        response.setLongitude(longitude);
        response.setRadiusMeters(resolvedRadiusMeters);
        response.setTransports(transports);
        return response;
    }

    private void issueEmailVerificationCode(User user) {
        String codeValue = generateVerificationCode();

        EmailVerificationCode verificationCode = new EmailVerificationCode();
        verificationCode.setEmail(user.getEmail());
        verificationCode.setCode(codeValue);
        verificationCode.setExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES));
        verificationCode.setConsumed(false);
        emailVerificationCodeRepository.save(verificationCode);

        try {
            emailService.sendVerificationCode(user.getEmail(), codeValue);
        } catch (MailException e) {
            throw new IllegalArgumentException("Failed to send verification email: " + e.getMessage());
        }
    }

    private String generateVerificationCode() {
        int code = new Random().nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    private Map<UUID, Long> loadVisibleStopCounts(Iterable<UUID> transportIds) {
        List<UUID> ids = new ArrayList<>();
        transportIds.forEach(ids::add);
        if (ids.isEmpty()) {
            return Map.of();
        }

        return transportRouteStopRepository.countVisibleStopsByTransportIds(ids).stream()
            .collect(Collectors.toMap(
                TransportCountProjection::getTransportId,
                TransportCountProjection::getCount
            ));
    }

    private Map<UUID, Long> loadVisibleDepartureCounts(Iterable<UUID> transportIds) {
        List<UUID> ids = new ArrayList<>();
        transportIds.forEach(ids::add);
        if (ids.isEmpty()) {
            return Map.of();
        }

        return transportDepartureSlotRepository.countVisibleDeparturesByTransportIds(ids).stream()
            .collect(Collectors.toMap(
                TransportCountProjection::getTransportId,
                TransportCountProjection::getCount
            ));
    }

    private User requireClientUser(UUID clientId) {
        User user = userRepository.findById(clientId)
            .orElseThrow(() -> new IllegalArgumentException("Client account not found"));

        if (user.getRole() != Role.CLIENT) {
            throw new IllegalArgumentException("The requested user is not a client account");
        }

        return user;
    }

    private ClientProfile requireClientProfile(UUID clientId) {
        return clientProfileRepository.findById(clientId)
            .orElseThrow(() -> new IllegalArgumentException("Client profile not found"));
    }

    private Transport requireTransport(UUID transportId) {
        return transportRepository.findById(transportId)
            .orElseThrow(() -> new IllegalArgumentException("Transport not found"));
    }

    private Pageable buildPageable(int page, int size, String sortExpression) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }

        int resolvedSize = size <= 0 ? DEFAULT_PAGE_SIZE : size;
        if (resolvedSize > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size must be less than or equal to " + MAX_PAGE_SIZE);
        }

        return PageRequest.of(page, resolvedSize, buildSort(sortExpression));
    }

    private Sort buildSort(String sortExpression) {
        String normalizedSort = normalizeSortExpression(sortExpression);
        String[] parts = normalizedSort.split(",", 2);
        String property = mapSortProperty(parts[0]);
        Sort.Direction direction = parseSortDirection(parts[1]);
        return Sort.by(direction, property);
    }

    private String normalizeSortExpression(String sortExpression) {
        String normalized = normalizeToNull(sortExpression);
        if (normalized == null) {
            return DEFAULT_SORT;
        }

        String[] parts = normalized.split(",", 2);
        String field = normalizeToNull(parts[0]);
        if (field == null) {
            throw new IllegalArgumentException("sort field is required");
        }

        String direction = parts.length > 1 ? normalizeToNull(parts[1]) : "asc";
        if (direction == null) {
            direction = "asc";
        }

        mapSortProperty(field);
        parseSortDirection(direction);
        return field + "," + direction.toLowerCase(Locale.ENGLISH);
    }

    private String mapSortProperty(String sortField) {
        return switch (sortField.trim().toLowerCase(Locale.ENGLISH)) {
            case "code" -> "code";
            case "name" -> "name";
            case "type" -> "type";
            case "route", "routename", "route_name" -> "route_name";
            case "startpoint", "start_point" -> "start_point";
            case "endpoint", "end_point" -> "end_point";
            case "zone" -> "zone";
            case "operatingzone", "operating_zone" -> "operating_zone";
            case "active" -> "active";
            default -> throw new IllegalArgumentException("Unsupported sort field: " + sortField);
        };
    }

    private Sort.Direction parseSortDirection(String direction) {
        try {
            return Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported sort direction: " + direction);
        }
    }

    private TransportType parseTransportType(String type) {
        String normalized = normalizeToNull(type);
        if (normalized == null) {
            return null;
        }

        try {
            return TransportType.valueOf(normalized.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid transport type. Allowed values are: BUS, TRAIN, METRO");
        }
    }

    private ClientAccountResponse buildAccountResponse(User user, ClientProfile profile, String message) {
        ClientAccountResponse response = new ClientAccountResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());
        response.setLastLoginAt(user.getLastLoginAt());
        response.setUsername(profile.getUsername());
        response.setFirstName(profile.getFirstName());
        response.setLastName(profile.getLastName());
        response.setPhone(profile.getPhone());
        response.setBirthDate(profile.getBirthDate());
        return response;
    }

    private ClientTransportDto toTransportDto(Transport transport, boolean includeAvailableDays) {
        return toTransportDto(transport, includeAvailableDays, null, null);
    }

    private ClientTransportDto toTransportDto(Transport transport,
                                               boolean includeAvailableDays,
                                               Map<UUID, Long> stopCounts,
                                               Map<UUID, Long> departureCounts) {
        ClientTransportDto dto = new ClientTransportDto();
        dto.setId(transport.getId());
        dto.setCode(transport.getCode());
        dto.setName(transport.getName());
        dto.setType(transport.getType() != null ? transport.getType().name() : null);
        dto.setRouteName(transport.getRoute_name());
        dto.setStartPoint(transport.getStart_point());
        dto.setEndPoint(transport.getEnd_point());
        dto.setZone(transport.getZone());
        dto.setOperatingZone(transport.getOperating_zone());
        dto.setActive(transport.getActive());
        dto.setStopCount(resolveStopCount(transport.getId(), stopCounts));
        dto.setDepartureCount(resolveDepartureCount(transport.getId(), departureCounts));
        if (includeAvailableDays) {
            dto.setAvailableDays(resolveAvailableDays(transport.getId()));
        }
        return dto;
    }

    private long resolveStopCount(UUID transportId, Map<UUID, Long> stopCounts) {
        if (stopCounts != null && stopCounts.containsKey(transportId)) {
            return stopCounts.get(transportId);
        }

        return transportRouteStopRepository.countVisibleStopsByTransportIds(List.of(transportId)).stream()
            .mapToLong(TransportCountProjection::getCount)
            .findFirst()
            .orElse(0L);
    }

    private long resolveDepartureCount(UUID transportId, Map<UUID, Long> departureCounts) {
        if (departureCounts != null && departureCounts.containsKey(transportId)) {
            return departureCounts.get(transportId);
        }

        return transportDepartureSlotRepository.countVisibleDeparturesByTransportIds(List.of(transportId)).stream()
            .mapToLong(TransportCountProjection::getCount)
            .findFirst()
            .orElse(0L);
    }

    private RouteContext resolveRouteContext(UUID transportId, UUID fromStopId, UUID toStopId) {
        if (transportId == null) {
            throw new IllegalArgumentException("transportId is required");
        }
        if (fromStopId == null) {
            throw new IllegalArgumentException("fromStopId is required");
        }
        if (toStopId == null) {
            throw new IllegalArgumentException("toStopId is required");
        }
        if (fromStopId.equals(toStopId)) {
            throw new IllegalArgumentException("fromStopId and toStopId must be different");
        }

        Transport transport = requireTransport(transportId);
        if (Boolean.FALSE.equals(transport.getActive())) {
            throw new IllegalArgumentException("Transport is inactive");
        }

        Map<UUID, TransportRouteStop> routeStopsById = transportRouteStopRepository
            .findByTransportIdOrderByStopOrderAsc(transportId)
            .stream()
            .filter(this::isClientVisibleRouteStop)
            .collect(Collectors.toMap(routeStop -> routeStop.getStop().getId(), routeStop -> routeStop));

        TransportRouteStop fromRouteStop = routeStopsById.get(fromStopId);
        TransportRouteStop toRouteStop = routeStopsById.get(toStopId);

        if (fromRouteStop == null) {
            throw new IllegalArgumentException("fromStopId does not belong to the selected transport route");
        }
        if (toRouteStop == null) {
            throw new IllegalArgumentException("toStopId does not belong to the selected transport route");
        }
        if (toRouteStop.getStopOrder() <= fromRouteStop.getStopOrder()) {
            throw new IllegalArgumentException("toStopId must be after fromStopId in the route order");
        }

        return new RouteContext(
            transport,
            fromRouteStop.getStop(),
            toRouteStop.getStop(),
            fromRouteStop.getStopOrder(),
            toRouteStop.getStopOrder(),
            toRouteStop.getStopOrder() - fromRouteStop.getStopOrder()
        );
    }

    private void validateDepartureEligibility(RouteContext routeContext, LocalTime selectedDepartureTime) {
        String today = LocalDate.now().getDayOfWeek().name();

        if (shiftStopEventRepository.existsByWorkShiftTransportIdAndWorkShiftStatusIgnoreCaseAndStopIdAndStatusIgnoreCase(
            routeContext.transport().getId(),
            WORK_SHIFT_STATUS_IN_PROGRESS,
            routeContext.fromStop().getId(),
            STOP_EVENT_STATUS_DEPARTED
        )) {
            throw new TicketEligibilityException("Boarding stop is no longer accessible: transport already departed from this stop");
        }

        List<TransportDepartureSlot> daySlots = transportDepartureSlotRepository
            .findByTransportIdAndDayOfWeekIgnoreCaseOrderByStopOrderAscDepartureTimeAsc(routeContext.transport().getId(), today)
            .stream()
            .filter(this::isClientVisibleDeparture)
            .toList();

        List<LocalTime> fromTimes = daySlots.stream()
            .filter(slot -> slot.getStop().getId().equals(routeContext.fromStop().getId()))
            .filter(slot -> slot.getStopOrder() != null && slot.getStopOrder().equals(routeContext.fromStopOrder()))
            .map(TransportDepartureSlot::getDepartureTime)
            .sorted()
            .toList();

        if (fromTimes.isEmpty()) {
            throw new TicketEligibilityException("No scheduled departure at selected boarding stop for today");
        }

        List<LocalTime> toTimes = daySlots.stream()
            .filter(slot -> slot.getStop().getId().equals(routeContext.toStop().getId()))
            .filter(slot -> slot.getStopOrder() != null && slot.getStopOrder().equals(routeContext.toStopOrder()))
            .map(TransportDepartureSlot::getDepartureTime)
            .sorted()
            .toList();

        if (toTimes.isEmpty()) {
            throw new TicketEligibilityException("No scheduled departure at selected destination stop for today");
        }

        boolean hasSegmentToday = fromTimes.stream()
            .anyMatch(fromTime -> toTimes.stream().anyMatch(toTime -> !toTime.isBefore(fromTime)));
        if (!hasSegmentToday) {
            throw new TicketEligibilityException("Selected route segment is not scheduled for today");
        }

        // Hard safety check: purchase must still be valid at the current server time.
        LocalTime now = LocalTime.now();
        boolean withinCurrentWindow = fromTimes.stream()
            .anyMatch(fromTime -> !now.isAfter(fromTime.plusMinutes(MAX_DEPARTURE_DELAY_TOLERANCE_MINUTES)));
        if (!withinCurrentWindow) {
            throw new TicketEligibilityException("Selected departure window has already passed");
        }

        // Optional UX check when client sends a selected departure time.
        if (selectedDepartureTime != null) {
            boolean selectedTimeIsEligible = fromTimes.stream()
                .anyMatch(fromTime -> !selectedDepartureTime.isBefore(fromTime)
                    && !selectedDepartureTime.isAfter(fromTime.plusMinutes(MAX_DEPARTURE_DELAY_TOLERANCE_MINUTES)));
            if (!selectedTimeIsEligible) {
                throw new TicketEligibilityException("Selected departure time is no longer eligible");
            }
        }

        Integer transportCapacity = routeContext.transport().getCapacity();
        if (transportCapacity == null || transportCapacity <= 0) {
            throw new IllegalArgumentException("Transport capacity is not configured");
        }

        long usedCapacity = ticketPurchaseRepository
            .countByTransport_IdAndFromStop_IdAndToStop_IdAndStatusIgnoreCaseAndValidUntilAfter(
                routeContext.transport().getId(),
                routeContext.fromStop().getId(),
                routeContext.toStop().getId(),
                TICKET_STATUS_ACTIVE,
                Instant.now()
            );

        if (usedCapacity >= transportCapacity) {
            throw new TicketEligibilityException("No places left for the selected segment");
        }
    }

    private TicketProduct resolveTicketProduct(UUID productId) {
        if (productId != null) {
            return ticketProductRepository.findByIdAndActiveTrue(productId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket product not found or inactive"));
        }

        return ticketProductRepository.findByActiveTrueOrderByPriceAscValidDurationMinutesAscNameAsc().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No active ticket products available"));
    }

    private BigDecimal calculateRouteFare(TransportType transportType, int stopCount) {
        if (transportType == null) {
            throw new IllegalArgumentException("Transport type is required to calculate fare");
        }

        BigDecimal baseFare;
        BigDecimal perStopFare;
        switch (transportType) {
            case BUS -> {
                baseFare = new BigDecimal("0.80");
                perStopFare = new BigDecimal("0.25");
            }
            case METRO -> {
                baseFare = new BigDecimal("0.70");
                perStopFare = new BigDecimal("0.20");
            }
            case TRAIN -> {
                baseFare = new BigDecimal("1.00");
                perStopFare = new BigDecimal("0.35");
            }
            default -> throw new IllegalArgumentException("Unsupported transport type");
        }

        return baseFare
            .add(perStopFare.multiply(BigDecimal.valueOf(Math.max(stopCount, 1))))
            .setScale(2, RoundingMode.HALF_UP);
    }

    private ClientTicketProductDto toTicketProductDto(TicketProduct product, RouteContext routeContext) {
        ClientTicketProductDto dto = new ClientTicketProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(calculateRouteFare(routeContext.transport().getType(), routeContext.stopCount()));
        dto.setValidDurationMinutes(product.getValidDurationMinutes());
        dto.setActive(product.getActive());
        dto.setTransportId(routeContext.transport().getId());
        dto.setFromStopId(routeContext.fromStop().getId());
        dto.setToStopId(routeContext.toStop().getId());
        dto.setStopCount(routeContext.stopCount());
        return dto;
    }

    private ClientTicketDto toTicketDto(TicketPurchase purchase, PaymentTransaction payment) {
        ClientTicketDto dto = new ClientTicketDto();
        dto.setTicketId(purchase.getId());
        dto.setProductId(purchase.getProduct() != null ? purchase.getProduct().getId() : null);
        dto.setProductName(purchase.getProduct() != null ? purchase.getProduct().getName() : null);
        dto.setProductDescription(purchase.getProduct() != null ? purchase.getProduct().getDescription() : null);
        dto.setValidDurationMinutes(purchase.getProduct() != null ? purchase.getProduct().getValidDurationMinutes() : null);
        dto.setTransportId(purchase.getTransport() != null ? purchase.getTransport().getId() : null);
        dto.setTransportCode(purchase.getTransport() != null ? purchase.getTransport().getCode() : null);
        dto.setTransportName(purchase.getTransport() != null ? purchase.getTransport().getName() : null);
        dto.setTransportType(purchase.getTransport() != null && purchase.getTransport().getType() != null
            ? purchase.getTransport().getType().name()
            : null);
        dto.setFromStopId(purchase.getFromStop() != null ? purchase.getFromStop().getId() : null);
        dto.setFromStopName(purchase.getFromStop() != null ? purchase.getFromStop().getStopName() : null);
        dto.setToStopId(purchase.getToStop() != null ? purchase.getToStop().getId() : null);
        dto.setToStopName(purchase.getToStop() != null ? purchase.getToStop().getStopName() : null);
        dto.setStopCount(purchase.getStopCount());
        dto.setStatus(purchase.getStatus());
        dto.setValidUntil(purchase.getValidUntil());
        dto.setPurchaseTime(purchase.getPurchaseTime());

        if (payment != null) {
            ClientTicketPaymentDto paymentDto = new ClientTicketPaymentDto();
            paymentDto.setPaymentId(payment.getId());
            paymentDto.setProvider(payment.getProvider());
            paymentDto.setProviderReference(payment.getProviderReference());
            paymentDto.setPaymentMethod(payment.getPaymentMethod());
            paymentDto.setProcessedAt(payment.getProcessedAt());
            paymentDto.setAmount(payment.getAmount());
            paymentDto.setStatus(payment.getStatus());
            dto.setPayment(paymentDto);
            dto.setPrice(payment.getAmount());
        }

        return dto;
    }

    private Pageable buildTicketPageable(int page, int size, String sortExpression) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }

        int resolvedSize = size <= 0 ? DEFAULT_PAGE_SIZE : size;
        if (resolvedSize > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size must be less than or equal to " + MAX_PAGE_SIZE);
        }

        String normalizedSort = normalizeTicketSortExpression(sortExpression);
        String[] parts = normalizedSort.split(",", 2);
        String property = mapTicketSortProperty(parts[0]);
        Sort.Direction direction = parseSortDirection(parts[1]);
        return PageRequest.of(page, resolvedSize, Sort.by(direction, property));
    }

    private String normalizeTicketSortExpression(String sortExpression) {
        String normalized = normalizeToNull(sortExpression);
        if (normalized == null) {
            return "purchaseTime,desc";
        }

        String[] parts = normalized.split(",", 2);
        String field = normalizeToNull(parts[0]);
        if (field == null) {
            throw new IllegalArgumentException("sort field is required");
        }

        String direction = parts.length > 1 ? normalizeToNull(parts[1]) : "desc";
        if (direction == null) {
            direction = "desc";
        }

        mapTicketSortProperty(field);
        parseSortDirection(direction);
        return field + "," + direction.toLowerCase(Locale.ENGLISH);
    }

    private String mapTicketSortProperty(String sortField) {
        return switch (sortField.trim().toLowerCase(Locale.ENGLISH)) {
            case "purchasetime", "purchase_time" -> "purchaseTime";
            case "validuntil", "valid_until" -> "validUntil";
            case "status" -> "status";
            default -> throw new IllegalArgumentException("Unsupported sort field: " + sortField);
        };
    }

    private String resolveRequiredOrDefault(String value, String defaultValue) {
        String normalized = normalizeToNull(value);
        return normalized != null ? normalized : defaultValue;
    }

    private List<String> resolveAvailableDays(UUID transportId) {
        return transportDepartureSlotRepository.findByTransportIdOrderByStopOrderAscDayOfWeekAscDepartureTimeAsc(transportId).stream()
            .filter(this::isClientVisibleDeparture)
            .map(slot -> normalizeDisplayDayOfWeek(slot.getDayOfWeek()))
            .distinct()
            .sorted(Comparator.comparingInt(day -> DayOfWeek.valueOf(day.toUpperCase(Locale.ENGLISH)).getValue()))
            .toList();
    }

    private ClientTransportStopDto toTransportStopDto(TransportRouteStop routeStop) {
        TransportStop stop = routeStop.getStop();

        ClientTransportStopDto dto = new ClientTransportStopDto();
        dto.setStopId(stop.getId().toString());
        dto.setStopOrder(routeStop.getStopOrder());
        dto.setStopName(stop.getStopName());
        dto.setZone(stop.getZone());
        dto.setActive(stop.getActive());
        dto.setLatitude(stop.getLatitude());
        dto.setLongitude(stop.getLongitude());
        return dto;
    }

    private ClientTransportDepartureDto toTransportDepartureDto(TransportDepartureSlot slot) {
        ClientTransportDepartureDto dto = new ClientTransportDepartureDto();
        dto.setStopId(slot.getStop().getId().toString());
        dto.setStopName(slot.getStop().getStopName());
        dto.setStopOrder(slot.getStopOrder());
        dto.setDepartureTime(slot.getDepartureTime());
        dto.setActive(slot.getActive());
        return dto;
    }

    private boolean isClientVisibleRouteStop(TransportRouteStop routeStop) {
        return routeStop != null
            && !Boolean.FALSE.equals(routeStop.getActive())
            && routeStop.getStop() != null
            && !Boolean.FALSE.equals(routeStop.getStop().getActive());
    }

    private boolean isClientVisibleDeparture(TransportDepartureSlot slot) {
        return slot != null
            && !Boolean.FALSE.equals(slot.getActive())
            && slot.getStop() != null
            && !Boolean.FALSE.equals(slot.getStop().getActive());
    }

    private boolean isEligibleNearbyRouteStop(TransportRouteStop routeStop) {
        return isClientVisibleRouteStop(routeStop)
            && routeStop.getTransport() != null
            && !Boolean.FALSE.equals(routeStop.getTransport().getActive())
            && routeStop.getStop().getLatitude() != null
            && routeStop.getStop().getLongitude() != null;
    }

    private BoundingBox buildBoundingBox(double latitude, double longitude, int radiusMeters) {
        double latitudeDelta = radiusMeters / 111_320d;
        double longitudeScale = Math.max(Math.cos(Math.toRadians(latitude)), 0.000001d);
        double longitudeDelta = radiusMeters / (111_320d * longitudeScale);

        return new BoundingBox(
            latitude - latitudeDelta,
            latitude + latitudeDelta,
            longitude - longitudeDelta,
            longitude + longitudeDelta
        );
    }

    private void validateCoordinates(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("lat must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("lng must be between -180 and 180");
        }
    }

    private double calculateDistanceMeters(double fromLatitude,
                                           double fromLongitude,
                                           double toLatitude,
                                           double toLongitude) {
        double latDistanceRadians = Math.toRadians(toLatitude - fromLatitude);
        double lngDistanceRadians = Math.toRadians(toLongitude - fromLongitude);

        double a = Math.sin(latDistanceRadians / 2) * Math.sin(latDistanceRadians / 2)
            + Math.cos(Math.toRadians(fromLatitude)) * Math.cos(Math.toRadians(toLatitude))
            * Math.sin(lngDistanceRadians / 2) * Math.sin(lngDistanceRadians / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }

    private double roundDistance(double distanceMeters) {
        return Math.round(distanceMeters * 100.0d) / 100.0d;
    }

    private String buildDisplayName(ClientProfile profile) {
        String firstName = normalizeToNull(profile.getFirstName());
        String lastName = normalizeToNull(profile.getLastName());

        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        if (firstName != null) {
            return firstName;
        }
        if (lastName != null) {
            return lastName;
        }

        String username = normalizeToNull(profile.getUsername());
        return username != null ? username : "Client";
    }

    private List<String> collectMissingProfileFields(ClientProfile profile) {
        List<String> missingFields = new ArrayList<>();

        if (normalizeToNull(profile.getUsername()) == null) {
            missingFields.add("username");
        }
        if (normalizeToNull(profile.getFirstName()) == null) {
            missingFields.add("firstName");
        }
        if (normalizeToNull(profile.getLastName()) == null) {
            missingFields.add("lastName");
        }
        if (profile.getBirthDate() == null) {
            missingFields.add("birthDate");
        }

        return missingFields;
    }

    private String normalizeDayOfWeek(String dayOfWeek) {
        String normalized = normalizeToNull(dayOfWeek);
        if (normalized == null) {
            throw new IllegalArgumentException("Day of week is required");
        }
        return DayOfWeek.valueOf(normalized.toUpperCase(Locale.ENGLISH)).name();
    }

    private String normalizeDisplayDayOfWeek(String dayOfWeek) {
        String normalized = normalizeToNull(dayOfWeek);
        if (normalized == null) {
            throw new IllegalArgumentException("Day of week is required");
        }
        return DayOfWeek.valueOf(normalized.toUpperCase(Locale.ENGLISH)).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

    private String normalizeToLowerCase(String value) {
        String normalized = normalizeToNull(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ENGLISH);
    }

    private String normalizeToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static final class NearbyCandidate {
        private final Transport transport;
        private TransportRouteStop nearestStop;
        private double distanceMeters;
        private int matchCount;

        private NearbyCandidate(Transport transport, TransportRouteStop nearestStop, double distanceMeters, int matchCount) {
            this.transport = transport;
            this.nearestStop = nearestStop;
            this.distanceMeters = distanceMeters;
            this.matchCount = matchCount;
        }

        public Transport transport() {
            return transport;
        }

        public TransportRouteStop nearestStop() {
            return nearestStop;
        }

        public void setNearestStop(TransportRouteStop nearestStop) {
            this.nearestStop = nearestStop;
        }

        public double distanceMeters() {
            return distanceMeters;
        }

        public void setDistanceMeters(double distanceMeters) {
            this.distanceMeters = distanceMeters;
        }

        public int matchCount() {
            return matchCount;
        }

        public void incrementMatchCount() {
            this.matchCount++;
        }
    }

    private record BoundingBox(double minLatitude, double maxLatitude, double minLongitude, double maxLongitude) {
    }

    private record RouteContext(Transport transport,
                                TransportStop fromStop,
                                TransportStop toStop,
                                int fromStopOrder,
                                int toStopOrder,
                                int stopCount) {
    }

    private static final class TicketEligibilityException extends IllegalArgumentException {
        private TicketEligibilityException(String message) {
            super(message);
        }
    }
}
