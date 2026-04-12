package com.tuniway.connect.model.dto;

public class UpdatedEmployeeResponse {
    private boolean success;
    private String message;
    private java.util.UUID id;
    private String email;
    private com.tuniway.connect.model.entity.Role role;
    private com.tuniway.connect.model.entity.AccountStatus status;
    private java.time.Instant createdAt;
    private String fullName;
    private String phone;
    private String license_number;
    private String employee_code;
    private String admin_code;

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

    public java.util.UUID getId() {
        return id;
    }

    public void setId(java.util.UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public com.tuniway.connect.model.entity.Role getRole() {
        return role;
    }

    public void setRole(com.tuniway.connect.model.entity.Role role) {
        this.role = role;
    }

    public com.tuniway.connect.model.entity.AccountStatus getStatus() {
        return status;
    }

    public void setStatus(com.tuniway.connect.model.entity.AccountStatus status) {
        this.status = status;
    }

    public java.time.Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLicense_number() {
        return license_number;
    }

    public void setLicense_number(String license_number) {
        this.license_number = license_number;
    }

    public String getEmployee_code() {
        return employee_code;
    }

    public void setEmployee_code(String employee_code) {
        this.employee_code = employee_code;
    }

    public String getAdmin_code() {
        return admin_code;
    }

    public void setAdmin_code(String admin_code) {
        this.admin_code = admin_code;
    }
}
