package com.tuniway.connect.model.dto;

import java.time.Instant;
import java.util.UUID;

public class AdminShiftResponse {
    private boolean success;
    private String message;
    private UUID shiftId;
    private UUID employeeId;
    private UUID transportId;
    private String transportName;
    private Instant scheduleStart;
    private Instant scheduleEnd;
    private String status;
    private Instant actualStart;
    private Instant actualEnd;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UUID getShiftId() {
        return shiftId;
    }

    public void setShiftId(UUID shiftId) {
        this.shiftId = shiftId;
    }

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

    public String getTransportName() {
        return transportName;
    }

    public void setTransportName(String transportName) {
        this.transportName = transportName;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getActualStart() {
        return actualStart;
    }

    public void setActualStart(Instant actualStart) {
        this.actualStart = actualStart;
    }

    public Instant getActualEnd() {
        return actualEnd;
    }

    public void setActualEnd(Instant actualEnd) {
        this.actualEnd = actualEnd;
    }
}
