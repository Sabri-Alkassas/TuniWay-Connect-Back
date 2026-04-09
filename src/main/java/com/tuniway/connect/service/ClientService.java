package com.tuniway.connect.service;

import com.tuniway.connect.model.dto.ClientAccountResponse;
import com.tuniway.connect.model.dto.ClientDashboardResponse;
import com.tuniway.connect.model.dto.ClientNearbyTransportsResponse;
import com.tuniway.connect.model.dto.ClientTransportDepartureDto;
import com.tuniway.connect.model.dto.ClientTransportDeparturesResponse;
import com.tuniway.connect.model.dto.ClientTransportDetailsResponse;
import com.tuniway.connect.model.dto.ClientTransportDto;
import com.tuniway.connect.model.dto.ClientTransportSearchResponse;
import com.tuniway.connect.model.dto.ClientTransportStopDto;
import com.tuniway.connect.model.dto.ClientTransportStopsResponse;
import com.tuniway.connect.model.dto.UpdateClientAccountRequest;
import com.tuniway.connect.model.entity.AccountStatus;
import com.tuniway.connect.model.entity.ClientProfile;
import com.tuniway.connect.model.entity.EmailVerificationCode;
import com.tuniway.connect.model.entity.Role;
import com.tuniway.connect.model.entity.Transport;
import com.tuniway.connect.model.entity.TransportDepartureSlot;
import com.tuniway.connect.model.entity.TransportRouteStop;
import com.tuniway.connect.model.entity.TransportStop;
import com.tuniway.connect.model.entity.TransportType;
import com.tuniway.connect.model.entity.User;
import com.tuniway.connect.repository.ClientProfileRepository;
import com.tuniway.connect.repository.EmailVerificationCodeRepository;
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

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

@Service
public class ClientService {
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_RADIUS_METERS = 500;
    private static final String DEFAULT_SORT = "name,asc";
    private static final double EARTH_RADIUS_METERS = 6_371_000d;

    private final UserRepository userRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final EmailVerificationCodeRepository emailVerificationCodeRepository;
    private final EmailService emailService;
    private final TransportRepository transportRepository;
    private final TransportRouteStopRepository transportRouteStopRepository;
    private final TransportDepartureSlotRepository transportDepartureSlotRepository;

    public ClientService(
        UserRepository userRepository,
        ClientProfileRepository clientProfileRepository,
        EmailVerificationCodeRepository emailVerificationCodeRepository,
        EmailService emailService,
        TransportRepository transportRepository,
        TransportRouteStopRepository transportRouteStopRepository,
        TransportDepartureSlotRepository transportDepartureSlotRepository
    ) {
        this.userRepository = userRepository;
        this.clientProfileRepository = clientProfileRepository;
        this.emailVerificationCodeRepository = emailVerificationCodeRepository;
        this.emailService = emailService;
        this.transportRepository = transportRepository;
        this.transportRouteStopRepository = transportRouteStopRepository;
        this.transportDepartureSlotRepository = transportDepartureSlotRepository;
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

        ClientTransportSearchResponse response = new ClientTransportSearchResponse();
        response.setSuccess(true);
        response.setMessage("Client transports retrieved successfully");
        response.setTransports(transports.getContent().stream().map(transport -> toTransportDto(transport, false)).toList());
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
        String dayOfWeek = date.getDayOfWeek().name();

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

    public ClientNearbyTransportsResponse getNearbyTransports(double latitude, double longitude, Integer radiusMeters) {
        validateCoordinates(latitude, longitude);

        int resolvedRadiusMeters = radiusMeters != null ? radiusMeters : DEFAULT_RADIUS_METERS;
        if (resolvedRadiusMeters <= 0) {
            throw new IllegalArgumentException("radiusMeters must be greater than 0");
        }

        List<NearbyCandidate> nearbyCandidates = new ArrayList<>();
        for (TransportRouteStop routeStop : transportRouteStopRepository.findAllWithTransportAndStop()) {
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

            NearbyCandidate existingCandidate = nearbyCandidates.stream()
                .filter(candidate -> candidate.transport().getId().equals(routeStop.getTransport().getId()))
                .findFirst()
                .orElse(null);

            if (existingCandidate == null) {
                nearbyCandidates.add(new NearbyCandidate(routeStop.getTransport(), routeStop, distanceMeters, 1));
                continue;
            }

            existingCandidate.incrementMatchCount();
            if (distanceMeters < existingCandidate.distanceMeters()) {
                existingCandidate.setNearestStop(routeStop);
                existingCandidate.setDistanceMeters(distanceMeters);
            }
        }

        List<ClientNearbyTransportsResponse.NearbyTransportDto> transports = nearbyCandidates.stream()
            .sorted(Comparator
                .comparingDouble(NearbyCandidate::distanceMeters)
                .thenComparing(candidate -> candidate.transport().getName(), String.CASE_INSENSITIVE_ORDER))
            .map(candidate -> {
                ClientNearbyTransportsResponse.NearbyTransportDto dto =
                    new ClientNearbyTransportsResponse.NearbyTransportDto();
                dto.setTransport(toTransportDto(candidate.transport(), false));
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
        dto.setStopCount(transportRouteStopRepository.countByTransportIdAndActiveTrue(transport.getId()));
        dto.setDepartureCount(transportDepartureSlotRepository.countByTransportIdAndActiveTrue(transport.getId()));
        if (includeAvailableDays) {
            dto.setAvailableDays(resolveAvailableDays(transport.getId()));
        }
        return dto;
    }

    private List<String> resolveAvailableDays(UUID transportId) {
        return transportDepartureSlotRepository.findByTransportIdOrderByStopOrderAscDayOfWeekAscDepartureTimeAsc(transportId).stream()
            .filter(this::isClientVisibleDeparture)
            .map(slot -> normalizeDayOfWeek(slot.getDayOfWeek()))
            .distinct()
            .sorted(Comparator.comparingInt(day -> DayOfWeek.valueOf(day).getValue()))
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
}
