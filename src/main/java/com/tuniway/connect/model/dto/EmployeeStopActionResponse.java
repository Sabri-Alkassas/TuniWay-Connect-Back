package com.tuniway.connect.model.dto;

import java.time.Instant;

public class EmployeeStopActionResponse {
    private String message;
    private String shiftId;
    private String stopId;
    private String status;
    private Instant arrivedAt;
    private Instant departedAt;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getArrivedAt() {
        return arrivedAt;
    }

    public void setArrivedAt(Instant arrivedAt) {
        this.arrivedAt = arrivedAt;
    }

    public Instant getDepartedAt() {
        return departedAt;
    }

    public void setDepartedAt(Instant departedAt) {
        this.departedAt = departedAt;
    }
}
