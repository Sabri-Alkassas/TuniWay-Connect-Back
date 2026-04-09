package com.tuniway.connect.model.dto;

import java.util.List;
import java.util.UUID;

public class ClientTransportDto {
    private UUID id;
    private String code;
    private String name;
    private String type;
    private String routeName;
    private String startPoint;
    private String endPoint;
    private String zone;
    private String operatingZone;
    private Boolean active;
    private long stopCount;
    private long departureCount;
    private List<String> availableDays;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(String startPoint) {
        this.startPoint = startPoint;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getOperatingZone() {
        return operatingZone;
    }

    public void setOperatingZone(String operatingZone) {
        this.operatingZone = operatingZone;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public long getStopCount() {
        return stopCount;
    }

    public void setStopCount(long stopCount) {
        this.stopCount = stopCount;
    }

    public long getDepartureCount() {
        return departureCount;
    }

    public void setDepartureCount(long departureCount) {
        this.departureCount = departureCount;
    }

    public List<String> getAvailableDays() {
        return availableDays;
    }

    public void setAvailableDays(List<String> availableDays) {
        this.availableDays = availableDays;
    }
}
