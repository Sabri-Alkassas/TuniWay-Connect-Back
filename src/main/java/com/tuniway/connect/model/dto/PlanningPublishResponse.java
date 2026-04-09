package com.tuniway.connect.model.dto;

import java.util.List;

public class PlanningPublishResponse {
    private boolean success;
    private String message;
    private int appliedCount;
    private List<AdminShiftResponse> shifts;

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

    public int getAppliedCount() {
        return appliedCount;
    }

    public void setAppliedCount(int appliedCount) {
        this.appliedCount = appliedCount;
    }

    public List<AdminShiftResponse> getShifts() {
        return shifts;
    }

    public void setShifts(List<AdminShiftResponse> shifts) {
        this.shifts = shifts;
    }
}
