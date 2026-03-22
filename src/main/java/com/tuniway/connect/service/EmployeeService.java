package com.tuniway.connect.service;

import com.tuniway.connect.model.dto.EmployeeScheduleResponse;
import com.tuniway.connect.model.dto.ShiftStartRequest;
import com.tuniway.connect.model.dto.ShiftStartResponse;
import com.tuniway.connect.model.entity.User;
import com.tuniway.connect.model.entity.WorkShift;
import com.tuniway.connect.repository.WorkShiftRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    @Autowired
    private WorkShiftRepository workShiftRepository;

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

    public ShiftStartResponse startShift(ShiftStartRequest request, UUID shiftId, User user) {
        WorkShift shift = workShiftRepository.findById(shiftId)
            .orElseThrow(() -> new RuntimeException("Work shift not found"));

        if (!shift.getEmployeeId().equals(user.getId())) {
            throw new RuntimeException("You are not assigned to this shift");
        }

        shift.setActualStart(java.time.Instant.now());
        shift.setStatus("IN_PROGRESS");
        workShiftRepository.save(shift);

        ShiftStartResponse response = new ShiftStartResponse();
        response.setSuccess(true);
        response.setMessage("Shift started at " + shift.getActualStart() + " successfully");
        response.setShift(shift);
        return response;
    }
}
