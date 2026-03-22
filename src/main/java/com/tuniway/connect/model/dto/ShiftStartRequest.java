package com.tuniway.connect.model.dto;

import java.util.UUID;

public class ShiftStartRequest {
    private UUID shiftId;

    public ShiftStartRequest() {
    }

    public ShiftStartRequest(UUID shiftId) {
        this.shiftId = shiftId;
    }

    public UUID getShiftId() {
        return shiftId;
    }

    public void setShiftId(UUID shiftId) {
        this.shiftId = shiftId;
    }
}
