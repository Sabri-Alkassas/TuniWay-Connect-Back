package com.tuniway.connect.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalTime;

public class ClientTransportDepartureDto {
    private String stopId;
    private String stopName;
    private Integer stopOrder;
    @JsonFormat(pattern = "HH:mm[:ss]")
    private LocalTime departureTime;
    private Boolean active;

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public Integer getStopOrder() {
        return stopOrder;
    }

    public void setStopOrder(Integer stopOrder) {
        this.stopOrder = stopOrder;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
