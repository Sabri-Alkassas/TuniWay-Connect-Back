package com.tuniway.connect.model.dto;


public class ShiftStartRequest {
    private Long shiftId;

    public ShiftStartRequest() {
    }

    public ShiftStartRequest(Long shiftId) {
        this.shiftId = shiftId;
    }

    public Long getShiftId() {
        return shiftId;
    }

    public void setShiftId(Long shiftId) {
        this.shiftId = shiftId;
    }
}
