package com.tuniway.connect.model.dto;

import java.time.Instant;
import java.util.UUID;

public class CreateShiftRequest {
    private UUID employeeId;
    private UUID transportId;
    private Instant scheduleStart;
    private Instant scheduleEnd;

    public UUID getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(UUID employeeId) {
        this.employeeId = employeeId;
    }

    public UUID getTransportId() {
        return transportId;
    }

    public void setTransportId(UUID transportId) {
        this.transportId = transportId;
    }

    public Instant getScheduleStart() {
        return scheduleStart;
    }

    public void setScheduleStart(Instant scheduleStart) {
        this.scheduleStart = scheduleStart;
    }

    public Instant getScheduleEnd() {
        return scheduleEnd;
    }

    public void setScheduleEnd(Instant scheduleEnd) {
        this.scheduleEnd = scheduleEnd;
    }
}
