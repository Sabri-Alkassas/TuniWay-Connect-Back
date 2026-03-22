package com.tuniway.connect.model.dto;

public class ShiftEndRequest {
    private Long shiftId;

    public ShiftEndRequest() {
    }

    public ShiftEndRequest(Long shiftId) {
        this.shiftId = shiftId;
    }

    public Long getShiftId() {
        return shiftId;
    }

    public void setShiftId(Long shiftId) {
        this.shiftId = shiftId;
    }
}
