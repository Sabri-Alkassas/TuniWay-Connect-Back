package com.tuniway.connect.model.dto;

import com.tuniway.connect.model.entity.TransportType;

public class TransportResponse {
    private String code;
    private TransportType type;
    private String message;
    private boolean success;

    public TransportResponse(String code, TransportType type, String message, boolean success) {
        this.code = code;
        this.type = type;
        this.message = message;
        this.success = success;
    }

    public TransportResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public TransportType getType() {
        return type;
    }
    public void setType(TransportType type) {
        this.type = type;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public boolean isSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }
}
