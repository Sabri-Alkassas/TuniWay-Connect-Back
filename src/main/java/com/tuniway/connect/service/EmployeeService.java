package com.tuniway.connect.service;

import com.tuniway.connect.model.dto.EmployeeScheduleResponse;
import com.tuniway.connect.model.dto.ShiftStartRequest;
import com.tuniway.connect.model.dto.ShiftStartResponse;
import com.tuniway.connect.model.dto.ShiftEndResponse;
import com.tuniway.connect.model.dto.EmployeeShiftStopsResponse;
import com.tuniway.connect.model.dto.EmployeeStopActionResponse;
import com.tuniway.connect.model.dto.ShiftStartRequest;
import com.tuniway.connect.model.dto.ShiftStartResponse;
import com.tuniway.connect.model.entity.ShiftStopEvent;
import com.tuniway.connect.model.entity.User;
import com.tuniway.connect.model.entity.WorkShift;
import com.tuniway.connect.repository.ShiftStopEventRepository;
import com.tuniway.connect.repository.WorkShiftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private static final Set<String> ALLOWED_STATUSES = new HashSet<>(
            Arrays.asList("pending", "arrived", "departed", "skipped")
    );

    @Autowired
    private WorkShiftRepository workShiftRepository;

    @Autowired
    private ShiftStopEventRepository shiftStopEventRepository;

    public EmployeeScheduleResponse getSchedule(UUID employeeId) {
        List<WorkShift> shifts = workShiftRepository.findByEmployeeIdOrderByScheduleStartAsc(employeeId);

        List<EmployeeScheduleResponse.ShiftDto> shiftDtos = shifts.stream()
                .map(shift -> new EmployeeScheduleResponse.ShiftDto(
                        shift.getId().toString(),
                        shift.getTransport().getId().toString(),
                        shift.getTransport().getName(),
                        shift.getTransport().getType(),
                        shift.getTransport().getZone(),
                        shift.getScheduleStart(),
                        shift.getScheduleEnd(),
                        shift.getStatus(),
                        shift.getActualStart(),
                        shift.getActualEnd()
                ))
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
        shift.setStatus("IN_PROGRESS");
        WorkShift saved = workShiftRepository.save(shift);
      
        return buildShiftStartResponse(saved, true, "Shift started at " + saved.getActualEnd() + " successfully");
    }

    public ShiftEndResponse endShift(UUID shiftId, User user) {
        WorkShift shift = workShiftRepository.findById(shiftId)
            .orElseThrow(() -> new RuntimeException("Work shift not found"));

        if (!shift.getEmployeeId().equals(user.getId())) {
            throw new RuntimeException("You are not assigned to this shift");
        }

        if (shift.getActualStart() == null) {
            throw new RuntimeException("Cannot end a shift that has not been started");
        }

        shift.setActualEnd(java.time.Instant.now());
        shift.setStatus("COMPLETED");
        workShiftRepository.save(shift);

        ShiftEndResponse response = new ShiftEndResponse();
        response.setSuccess(true);
        response.setMessage("Shift ended at " + shift.getActualEnd() + " successfully");
        response.setShift(shift);
        return response;
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
    public EmployeeStopActionResponse arriveAtStop(UUID employeeId, UUID shiftId, UUID stopId) {
        requireOwnedShift(employeeId, shiftId);
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

        return toStopActionResponse(updated, "Arrival recorded successfully");
    }

    @Transactional
    public EmployeeStopActionResponse departFromStop(UUID employeeId, UUID shiftId, UUID stopId) {
        requireOwnedShift(employeeId, shiftId);
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

        return toStopActionResponse(updated, "Departure recorded successfully");
    }

    private WorkShift requireOwnedShift(UUID employeeId, UUID shiftId) {
        return workShiftRepository.findByIdAndEmployeeId(shiftId, employeeId)
                .orElseThrow(() -> new RuntimeException("Shift not found for this employee"));
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

    private void validateKnownStatus(String status) {
        if (!ALLOWED_STATUSES.contains(status)) {
            throw new RuntimeException("Unsupported stop status: " + status);
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
}
