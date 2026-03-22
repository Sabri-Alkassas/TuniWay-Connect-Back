package com.tuniway.connect.model.dto;

import java.time.Instant;
import java.util.UUID;

public class ShiftEndResponse {
    private boolean success;
    private String message;
    private UUID shiftId;
    private String status;
    private Instant actualEnd;

    public UUID getShiftId() {
        return shiftId;
    }

    public void setShiftId(UUID shiftId) {
        this.shiftId = shiftId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getActualEnd() {
        return actualEnd;
    }

    public void setActualEnd(Instant actualEnd) {
        this.actualEnd = actualEnd;
    }

    public boolean getSuccess() {
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
}
