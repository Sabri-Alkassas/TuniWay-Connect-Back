package com.tuniway.connect.model.dto;

import java.util.UUID;

public class UpdatedEmployeeStatusRequest {
    private UUID employeeId;
    private String status;

    public UUID getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(UUID employeeId) {
        this.employeeId = employeeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
