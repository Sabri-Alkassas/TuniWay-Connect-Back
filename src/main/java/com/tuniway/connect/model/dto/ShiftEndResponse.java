package com.tuniway.connect.model.dto;

import com.tuniway.connect.model.entity.WorkShift;

public class ShiftEndResponse {
    private boolean success;
    private String message;
    private WorkShift shift;

    public WorkShift getShift() {
        return shift;
    }

    public void setShift(WorkShift shift) {
        this.shift = shift;
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
