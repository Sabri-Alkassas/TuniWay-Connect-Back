package com.tuniway.connect.service;

import com.tuniway.connect.model.dto.EmployeeLocationUpdateRequest;
import com.tuniway.connect.model.dto.EmployeeScheduleResponse;
import com.tuniway.connect.model.dto.EmployeeShiftLocationResponse;
import com.tuniway.connect.model.dto.EmployeeTicketValidationResponse;
import com.tuniway.connect.model.dto.ShiftStartRequest;
import com.tuniway.connect.model.dto.ShiftStartResponse;
import com.tuniway.connect.model.dto.ShiftEndResponse;
import com.tuniway.connect.model.dto.EmployeeShiftStopsResponse;
import com.tuniway.connect.model.dto.EmployeeStopActionResponse;
import com.tuniway.connect.model.dto.EmployeeShiftProgressResponse;
import com.tuniway.connect.model.dto.ValidateTicketRequest;
import com.tuniway.connect.model.entity.PaymentTransaction;
import com.tuniway.connect.model.entity.ShiftStopEvent;
import com.tuniway.connect.model.entity.TicketPurchase;
import com.tuniway.connect.model.entity.Transport;
import com.tuniway.connect.model.entity.User;
import com.tuniway.connect.model.entity.WorkShift;
import com.tuniway.connect.repository.PaymentTransactionRepository;
import com.tuniway.connect.repository.ShiftStopEventRepository;
import com.tuniway.connect.repository.TicketPurchaseRepository;
import com.tuniway.connect.repository.WorkShiftRepository;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private static final Set<String> ALLOWED_STATUSES = new HashSet<>(
        Arrays.asList("pending", "arrived", "departed", "skipped")
    );
    private final WorkShiftRepository workShiftRepository;
    private final ShiftStopEventRepository shiftStopEventRepository;
    private final TicketPurchaseRepository ticketPurchaseRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public EmployeeService(WorkShiftRepository workShiftRepository,
                        ShiftStopEventRepository shiftStopEventRepository,
                        TicketPurchaseRepository ticketPurchaseRepository,
                        PaymentTransactionRepository paymentTransactionRepository,
                        SimpMessagingTemplate messagingTemplate) {
        this.workShiftRepository = workShiftRepository;
        this.shiftStopEventRepository = shiftStopEventRepository;
        this.ticketPurchaseRepository = ticketPurchaseRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.messagingTemplate = messagingTemplate;
    }
    private void publishShiftProgressToEmployee(User user, UUID shiftId) {
        EmployeeShiftProgressResponse progress = getShiftProgress(user.getId(), shiftId);
        messagingTemplate.convertAndSendToUser(
                user.getEmail(),
                "/queue/shift-progress",
                progress
        );
    }
    public EmployeeScheduleResponse getSchedule(UUID employeeId) {
        List<WorkShift> shifts = workShiftRepository.findByEmployeeIdOrderByScheduleStartAsc(employeeId);

        List<EmployeeScheduleResponse.ShiftDto> shiftDtos = shifts.stream()
                .map(this::toScheduleShiftDto)
                .collect(Collectors.toList());

        EmployeeScheduleResponse response = new EmployeeScheduleResponse();
        response.setMessage("Schedule retrieved successfully");
        response.setShifts(shiftDtos);
        return response;
    }

    @Transactional
    public ShiftStartResponse startShift(ShiftStartRequest request, UUID shiftId, User user) {
        if (request != null && request.getShiftId() != null && !request.getShiftId().equals(shiftId)) {
            throw new RuntimeException("Shift id in body does not match URL path");
        }

        WorkShift shift = requireOwnedShift(user.getId(), shiftId);
        String status = normalizeShiftStatus(shift.getStatus());

        if ("IN_PROGRESS".equals(status)) {
            return buildShiftStartResponse(shift, true, "Shift already started");
        }

        if (!"SCHEDULED".equals(status)) {
            throw new RuntimeException("Shift cannot be started from status: " + status);
        }

        if (shift.getActualStart() == null) {
            shift.setActualStart(Instant.now());
        }
        shift.setCurrentLatitude(null);
        shift.setCurrentLongitude(null);
        shift.setCurrentLocationUpdatedAt(null);
        shift.setStatus("IN_PROGRESS");
        WorkShift saved = workShiftRepository.save(shift);
        publishShiftProgressToEmployee(user, shiftId);
      
        return buildShiftStartResponse(saved, true, "Shift started at " + saved.getActualStart() + " successfully");
    }

    public ShiftEndResponse endShift(UUID shiftId, User user) {
        WorkShift shift = workShiftRepository.findById(shiftId)
            .orElseThrow(() -> new RuntimeException("Work shift not found"));

        if (!shift.getEmployeeId().equals(user.getId())) {
            throw new RuntimeException("You are not assigned to this shift");
        }

        String status = normalizeShiftStatus(shift.getStatus());
        if ("COMPLETED".equals(status)) {
            ShiftEndResponse response = new ShiftEndResponse();
            response.setSuccess(true);
            response.setMessage("Shift already ended");
            response.setShiftId(shift.getId());
            response.setStatus(shift.getStatus());
            response.setActualEnd(shift.getActualEnd());
            return response;
        }

        if (shift.getActualStart() == null) {
            throw new RuntimeException("Cannot end a shift that has not been started");
        }
        if (!"IN_PROGRESS".equals(status)) {
            throw new RuntimeException("Shift cannot be ended from status: " + status);
        }

        shift.setActualEnd(java.time.Instant.now());
        shift.setStatus("COMPLETED");
        workShiftRepository.save(shift);
        publishShiftProgressToEmployee(user, shiftId);

        ShiftEndResponse response = new ShiftEndResponse();
        response.setSuccess(true);
        response.setMessage("Shift ended at " + shift.getActualEnd() + " successfully");
        response.setShiftId(shift.getId());
        response.setStatus(shift.getStatus());
        response.setActualEnd(shift.getActualEnd());
        return response;
    }

    @Transactional
    public EmployeeShiftLocationResponse updateShiftLocation(User user, UUID shiftId, EmployeeLocationUpdateRequest request) {
        if (request == null) {
            throw new RuntimeException("Location request body is required");
        }
        if (request.getLatitude() == null || request.getLongitude() == null) {
            throw new RuntimeException("Both latitude and longitude are required");
        }

        validateLocationCoordinates(request.getLatitude(), request.getLongitude());

        WorkShift shift = requireOwnedShift(user.getId(), shiftId);
        if (!"IN_PROGRESS".equals(normalizeShiftStatus(shift.getStatus()))) {
            throw new RuntimeException("Shift location can only be updated while the shift is in progress");
        }

        Instant now = Instant.now();
        shift.setCurrentLatitude(request.getLatitude());
        shift.setCurrentLongitude(request.getLongitude());
        shift.setCurrentLocationUpdatedAt(now);
        WorkShift saved = workShiftRepository.save(shift);
        publishShiftProgressToEmployee(user, shiftId);

        EmployeeShiftLocationResponse response = new EmployeeShiftLocationResponse();
        response.setSuccess(true);
        response.setMessage("Shift location updated successfully");
        response.setShiftId(saved.getId());
        response.setTransportId(saved.getTransport() != null ? saved.getTransport().getId() : null);
        response.setLatitude(saved.getCurrentLatitude());
        response.setLongitude(saved.getCurrentLongitude());
        response.setUpdatedAt(saved.getCurrentLocationUpdatedAt());
        return response;
    }

    @Transactional
    public EmployeeTicketValidationResponse validateTicket(User user, ValidateTicketRequest request) {
        if (request == null) {
            throw new RuntimeException("Validation request body is required");
        }
        if (request.getTicketId() == null) {
            throw new RuntimeException("ticketId is required");
        }

        String providedToken = request.getQrCode() == null ? "" : request.getQrCode().trim();
        if (providedToken.isEmpty()) {
            throw new RuntimeException("qrCode token is required");
        }

        WorkShift activeShift = resolveActiveShiftForValidation(user.getId(), request.getShiftId());

        TicketPurchase purchase = ticketPurchaseRepository.findByIdForUpdate(request.getTicketId())
            .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (purchase.getTransport() == null || purchase.getTransport().getId() == null) {
            throw new RuntimeException("Ticket is not linked to a transport");
        }
        if (activeShift.getTransport() == null || activeShift.getTransport().getId() == null) {
            throw new RuntimeException("Active shift transport is missing");
        }
        if (!purchase.getTransport().getId().equals(activeShift.getTransport().getId())) {
            throw new RuntimeException("Ticket does not belong to the active shift transport");
        }

        String currentStatus = purchase.getStatus() == null
            ? ""
            : purchase.getStatus().trim().toUpperCase(Locale.ENGLISH);

        if ("USED".equals(currentStatus)) {
            return buildValidationResponse(activeShift, purchase, false, "Ticket already validated", Instant.now());
        }
        if (purchase.getValidUntil() != null && purchase.getValidUntil().isBefore(Instant.now())) {
            throw new RuntimeException("Ticket is expired");
        }
        if (!"ACTIVE".equals(currentStatus)) {
            throw new RuntimeException("Ticket status is not valid for validation: " + currentStatus);
        }

        String expectedToken = resolveExpectedTicketToken(purchase);
        if (!expectedToken.equals(providedToken)) {
            throw new RuntimeException("Invalid QR token for this ticket");
        }

        Instant validatedAt = Instant.now();
        purchase.setStatus("USED");
        TicketPurchase saved = ticketPurchaseRepository.save(purchase);

        return buildValidationResponse(activeShift, saved, true, "Ticket validated successfully", validatedAt);
    }

    public EmployeeShiftStopsResponse getShiftStops(UUID employeeId, UUID shiftId) {
        WorkShift shift = requireOwnedShift(employeeId, shiftId);
        List<ShiftStopEvent> events = shiftStopEventRepository.findByWorkShiftIdOrderByStopOrderAsc(shift.getId());

        List<EmployeeShiftStopsResponse.ShiftStopDto> stopDtos = events.stream()
                .map(this::toShiftStopDto)
                .collect(Collectors.toList());

        EmployeeShiftStopsResponse response = new EmployeeShiftStopsResponse();
        response.setMessage("Shift stops retrieved successfully");
        response.setShiftId(shiftId.toString());
        response.setStops(stopDtos);
        return response;
    }

    @Transactional
    public EmployeeStopActionResponse arriveAtStop(User user, UUID shiftId, UUID stopId) {
        WorkShift shift = requireOwnedShift(user.getId(), shiftId);
        ensureShiftInProgress(shift);
        ShiftStopEvent event = requireShiftStopEvent(shiftId, stopId);
        String status = normalizeStatus(event.getStatus());
        validateKnownStatus(status);

        if ("departed".equals(status)) {
            throw new RuntimeException("Stop already departed. Arrival is no longer allowed.");
        }
        if ("skipped".equals(status)) {
            throw new RuntimeException("Stop is skipped. Arrival is not allowed.");
        }
        if ("arrived".equals(status)) {
            return toStopActionResponse(event, "Arrival already recorded");
        }

        Instant now = Instant.now();
        int updatedRows = shiftStopEventRepository.markArrivedIfPending(shiftId, stopId, now);
        if (updatedRows == 0) {
            ShiftStopEvent latest = requireShiftStopEvent(shiftId, stopId);
            String latestStatus = normalizeStatus(latest.getStatus());
            if ("arrived".equals(latestStatus)) {
                return toStopActionResponse(latest, "Arrival already recorded");
            }
            if ("departed".equals(latestStatus)) {
                throw new RuntimeException("Stop already departed. Arrival is no longer allowed.");
            }
            if ("skipped".equals(latestStatus)) {
                throw new RuntimeException("Stop is skipped. Arrival is not allowed.");
            }
            throw new RuntimeException("Arrival transition failed due to concurrent update. Retry request.");
        }

        ShiftStopEvent updated = requireShiftStopEvent(shiftId, stopId);
        publishShiftProgressToEmployee(user, shiftId);
        return toStopActionResponse(updated, "Arrival recorded successfully");
    }

    @Transactional
    public EmployeeStopActionResponse departFromStop(User user, UUID shiftId, UUID stopId) {
        WorkShift shift = requireOwnedShift(user.getId(), shiftId);
        ensureShiftInProgress(shift);
        ShiftStopEvent event = requireShiftStopEvent(shiftId, stopId);

        String status = normalizeStatus(event.getStatus());
        validateKnownStatus(status);

        if ("departed".equals(status)) {
            return toStopActionResponse(event, "Departure already recorded");
        }
        if ("skipped".equals(status)) {
            throw new RuntimeException("Stop is skipped. Departure is not allowed.");
        }

        Instant now = Instant.now();
        int updatedRows;
        if ("arrived".equals(status)) {
            updatedRows = shiftStopEventRepository.markDepartedIfArrived(shiftId, stopId, now);
        } else {
            updatedRows = shiftStopEventRepository.markDepartedDirectlyIfPending(shiftId, stopId, now);
        }

        if (updatedRows == 0) {
            ShiftStopEvent latest = requireShiftStopEvent(shiftId, stopId);
            String latestStatus = normalizeStatus(latest.getStatus());
            if ("departed".equals(latestStatus)) {
                return toStopActionResponse(latest, "Departure already recorded");
            }
            if ("skipped".equals(latestStatus)) {
                throw new RuntimeException("Stop is skipped. Departure is not allowed.");
            }
            throw new RuntimeException("Departure transition failed due to concurrent update. Retry request.");
        }

        ShiftStopEvent updated = requireShiftStopEvent(shiftId, stopId);
        publishShiftProgressToEmployee(user, shiftId);
        return toStopActionResponse(updated, "Departure recorded successfully");
    }

    private WorkShift requireOwnedShift(UUID employeeId, UUID shiftId) {
        return workShiftRepository.findByIdAndEmployeeId(shiftId, employeeId)
                .orElseThrow(() -> new RuntimeException("Shift not found for this employee"));
    }

    private WorkShift resolveActiveShiftForValidation(UUID employeeId, UUID requestedShiftId) {
        WorkShift shift;
        if (requestedShiftId != null) {
            shift = requireOwnedShift(employeeId, requestedShiftId);
        } else {
            shift = workShiftRepository
                .findFirstByEmployeeIdAndStatusIgnoreCaseOrderByActualStartDesc(employeeId, "IN_PROGRESS")
                .orElseThrow(() -> new RuntimeException("No active shift found for this employee"));
        }

        if (!"IN_PROGRESS".equals(normalizeShiftStatus(shift.getStatus()))) {
            throw new RuntimeException("Shift must be in progress for ticket validation");
        }

        return shift;
    }

    private String resolveExpectedTicketToken(TicketPurchase purchase) {
        return paymentTransactionRepository
            .findFirstByTicketPurchaseIdOrderByProcessedAtDesc(purchase.getId())
            .map(PaymentTransaction::getProviderReference)
            .map(String::trim)
            .filter(value -> !value.isEmpty())
            .orElseGet(() -> purchase.getId().toString());
    }

    private EmployeeTicketValidationResponse buildValidationResponse(WorkShift shift,
                                                                     TicketPurchase purchase,
                                                                     boolean consumedNow,
                                                                     String message,
                                                                     Instant validatedAt) {
        EmployeeTicketValidationResponse response = new EmployeeTicketValidationResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setShiftId(shift.getId());
        response.setTicketId(purchase.getId());
        response.setStatus(consumedNow ? "USED" : purchase.getStatus());
        response.setValidatedAt(validatedAt);
        return response;
    }

    private ShiftStopEvent requireShiftStopEvent(UUID shiftId, UUID stopId) {
        return shiftStopEventRepository.findByWorkShiftIdAndStopId(shiftId, stopId)
                .orElseThrow(() -> new RuntimeException("Stop not found for this shift"));
    }

    private String normalizeStatus(String status) {
        return status == null ? "pending" : status.trim().toLowerCase();
    }

    private String normalizeShiftStatus(String status) {
        return status == null ? "SCHEDULED" : status.trim().toUpperCase();
    }

    private void ensureShiftInProgress(WorkShift shift) {
        String status = normalizeShiftStatus(shift.getStatus());
        if (!"IN_PROGRESS".equals(status)) {
            throw new RuntimeException("Shift must be in progress for this action. Current status: " + status);
        }
    }

    private void validateKnownStatus(String status) {
        if (!ALLOWED_STATUSES.contains(status)) {
            throw new RuntimeException("Unsupported stop status: " + status);
        }
    }

    private void validateLocationCoordinates(BigDecimal latitude, BigDecimal longitude) {
        if (latitude.compareTo(BigDecimal.valueOf(-90)) < 0 || latitude.compareTo(BigDecimal.valueOf(90)) > 0) {
            throw new RuntimeException("Latitude must be between -90 and 90");
        }
        if (longitude.compareTo(BigDecimal.valueOf(-180)) < 0 || longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new RuntimeException("Longitude must be between -180 and 180");
        }
    }

    private EmployeeShiftStopsResponse.ShiftStopDto toShiftStopDto(ShiftStopEvent event) {
        EmployeeShiftStopsResponse.ShiftStopDto dto = new EmployeeShiftStopsResponse.ShiftStopDto();
        dto.setStopId(event.getStop().getId().toString());
        dto.setStopOrder(event.getStopOrder());
        dto.setStopName(event.getStop().getStopName());
        dto.setStatus(event.getStatus());
        dto.setExpectedDepartureTime(event.getExpectedDepartureTime());
        dto.setArrivedAt(event.getArrivedAt());
        dto.setDepartedAt(event.getDepartedAt());
        return dto;
    }

    private EmployeeStopActionResponse toStopActionResponse(ShiftStopEvent event, String message) {
        EmployeeStopActionResponse response = new EmployeeStopActionResponse();
        response.setMessage(message);
        response.setShiftId(event.getWorkShift().getId().toString());
        response.setStopId(event.getStop().getId().toString());
        response.setStatus(event.getStatus());
        response.setArrivedAt(event.getArrivedAt());
        response.setDepartedAt(event.getDepartedAt());
        return response;
    }

    private ShiftStartResponse buildShiftStartResponse(WorkShift shift, boolean success, String message) {
        ShiftStartResponse response = new ShiftStartResponse();
        response.setSuccess(success);
        response.setMessage(message);
        response.setShiftId(shift.getId());
        response.setStatus(shift.getStatus());
        response.setActualStart(shift.getActualStart());
        return response;
    }

    private EmployeeScheduleResponse.ShiftDto toScheduleShiftDto(WorkShift shift) {
        Transport transport = shift.getTransport();

        return new EmployeeScheduleResponse.ShiftDto(
                shift.getId().toString(),
                transport != null && transport.getId() != null ? transport.getId().toString() : null,
                transport != null ? transport.getName() : null,
                transport != null && transport.getType() != null ? transport.getType().name() : null,
                transport != null ? transport.getZone() : null,
                shift.getScheduleStart(),
                shift.getScheduleEnd(),
                normalizeShiftStatus(shift.getStatus()).toLowerCase(),
                shift.getActualStart(),
                shift.getActualEnd()
        );
    }

    public EmployeeShiftProgressResponse getShiftProgress(UUID employeeId, UUID shiftId) {
        WorkShift shift = requireOwnedShift(employeeId, shiftId);
        List<ShiftStopEvent> events = shiftStopEventRepository.findByWorkShiftIdOrderByStopOrderAsc(shift.getId());

        List<EmployeeShiftProgressResponse.ProgressStopDto> allStops = events.stream()
                .map(this::toProgressStopDto)
                .collect(Collectors.toList());

        List<EmployeeShiftProgressResponse.ProgressStopDto> completedStops = allStops.stream()
                .filter(stop -> {
                    String status = normalizeStatus(stop.getStatus());
                    return "departed".equals(status) || "skipped".equals(status);
                })
                .collect(Collectors.toList());

        EmployeeShiftProgressResponse.ProgressStopDto currentStop = allStops.stream()
                .filter(stop -> {
                    String status = normalizeStatus(stop.getStatus());
                    return "arrived".equals(status) && stop.getDepartedAt() == null;
                })
                .findFirst()
                .orElse(null);

        EmployeeShiftProgressResponse.ProgressStopDto nextStop;
        if (currentStop != null) {
            nextStop = allStops.stream()
                    .filter(stop -> stop.getStopOrder() != null
                            && currentStop.getStopOrder() != null
                            && stop.getStopOrder() > currentStop.getStopOrder())
                    .filter(stop -> "pending".equals(normalizeStatus(stop.getStatus())))
                    .findFirst()
                    .orElse(null);
        } else {
            nextStop = allStops.stream()
                    .filter(stop -> "pending".equals(normalizeStatus(stop.getStatus())))
                    .findFirst()
                    .orElse(null);
        }

        EmployeeShiftProgressResponse response = new EmployeeShiftProgressResponse();
        response.setMessage("Shift progress retrieved successfully");
        response.setShiftId(shift.getId().toString());
        response.setShiftStatus(normalizeShiftStatus(shift.getStatus()));
        response.setCurrentStop(currentStop);
        response.setCompletedStops(completedStops);
        response.setCompletedStopsCount(completedStops.size());
        response.setTotalStops(allStops.size());
        response.setNextStop(nextStop);
        response.setDelayMinutes(calculateDelayMinutes(shift, currentStop, nextStop));
        response.setCurrentLatitude(shift.getCurrentLatitude());
        response.setCurrentLongitude(shift.getCurrentLongitude());
        response.setCurrentLocationUpdatedAt(shift.getCurrentLocationUpdatedAt());

        return response;
    }
    private EmployeeShiftProgressResponse.ProgressStopDto toProgressStopDto(ShiftStopEvent event) {
    EmployeeShiftProgressResponse.ProgressStopDto dto = new EmployeeShiftProgressResponse.ProgressStopDto();
    dto.setStopId(event.getStop().getId().toString());
    dto.setStopOrder(event.getStopOrder());
    dto.setStopName(event.getStop().getStopName());
    dto.setStatus(event.getStatus());
    dto.setExpectedDepartureTime(event.getExpectedDepartureTime());
    dto.setArrivedAt(event.getArrivedAt());
    dto.setDepartedAt(event.getDepartedAt());
    return dto;
}

private Long calculateDelayMinutes(WorkShift shift,
                                    EmployeeShiftProgressResponse.ProgressStopDto currentStop,
                                    EmployeeShiftProgressResponse.ProgressStopDto nextStop) {
        Instant now = Instant.now();

        if ("SCHEDULED".equals(normalizeShiftStatus(shift.getStatus())) && shift.getScheduleStart() != null) {
            return Math.max(Duration.between(shift.getScheduleStart(), now).toMinutes(), 0L);
        }

        if (currentStop != null && currentStop.getExpectedDepartureTime() != null) {
            return Math.max(Duration.between(currentStop.getExpectedDepartureTime(), now).toMinutes(), 0L);
        }

        if (nextStop != null && nextStop.getExpectedDepartureTime() != null) {
            return Math.max(Duration.between(nextStop.getExpectedDepartureTime(), now).toMinutes(), 0L);
        }

        if ("COMPLETED".equals(normalizeShiftStatus(shift.getStatus()))) {
            return 0L;
        }

        return 0L;
    }
}
