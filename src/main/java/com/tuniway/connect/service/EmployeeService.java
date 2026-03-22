package com.tuniway.connect.service;

import com.tuniway.connect.model.dto.EmployeeScheduleResponse;
import com.tuniway.connect.model.dto.EmployeeShiftStopsResponse;
import com.tuniway.connect.model.dto.EmployeeStopActionResponse;
import com.tuniway.connect.model.entity.ShiftStopEvent;
import com.tuniway.connect.model.entity.WorkShift;
import com.tuniway.connect.repository.ShiftStopEventRepository;
import com.tuniway.connect.repository.WorkShiftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

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
        if ("departed".equals(status)) {
            throw new RuntimeException("Stop already departed. Arrival is no longer allowed.");
        }

        if ("arrived".equals(status)) {
            return toStopActionResponse(event, "Arrival already recorded");
        }

        Instant now = Instant.now();
        event.setArrivedAt(now);
        event.setStatus("arrived");
        event.setUpdatedAt(now);
        ShiftStopEvent updated = shiftStopEventRepository.save(event);

        return toStopActionResponse(updated, "Arrival recorded successfully");
    }

    @Transactional
    public EmployeeStopActionResponse departFromStop(UUID employeeId, UUID shiftId, UUID stopId) {
        requireOwnedShift(employeeId, shiftId);
        ShiftStopEvent event = requireShiftStopEvent(shiftId, stopId);

        String status = normalizeStatus(event.getStatus());
        if ("departed".equals(status)) {
            return toStopActionResponse(event, "Departure already recorded");
        }

        Instant now = Instant.now();
        if (!"arrived".equals(status)) {
            event.setArrivedAt(now);
        }
        event.setDepartedAt(now);
        event.setStatus("departed");
        event.setUpdatedAt(now);
        ShiftStopEvent updated = shiftStopEventRepository.save(event);

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
}
