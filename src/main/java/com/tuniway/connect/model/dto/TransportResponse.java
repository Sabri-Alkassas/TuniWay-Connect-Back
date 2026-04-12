package com.tuniway.connect.model.dto;

import com.tuniway.connect.model.entity.TransportType;

public class TransportResponse {
    private java.util.UUID id;
    private String code;
    private String name;
    private TransportType type;
    private String zone;
    private boolean active;
    private long stopsCount;
    private long departuresCount;
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

    public java.util.UUID getId() {
        return id;
    }
    public void setId(java.util.UUID id) {
        this.id = id;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public TransportType getType() {
        return type;
    }
    public void setType(TransportType type) {
        this.type = type;
    }
    public String getZone() {
        return zone;
    }
    public void setZone(String zone) {
        this.zone = zone;
    }
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
    public long getStopsCount() {
        return stopsCount;
    }
    public void setStopsCount(long stopsCount) {
        this.stopsCount = stopsCount;
    }
    public long getDeparturesCount() {
        return departuresCount;
    }
    public void setDeparturesCount(long departuresCount) {
        this.departuresCount = departuresCount;
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
