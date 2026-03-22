package com.tuniway.connect.model.dto;

public class LogoutResponse {
    private boolean success;
    private String message;

    public LogoutResponse() {
    }

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
}
