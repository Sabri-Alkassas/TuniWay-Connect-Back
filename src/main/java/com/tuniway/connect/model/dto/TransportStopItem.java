package com.tuniway.connect.model.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class TransportStopItem {
    private UUID stopId;
    private String stopName;
    private String zone;
    private Boolean active;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer stopOrder;

    public UUID getStopId() {
        return stopId;
    }

    public void setStopId(UUID stopId) {
        this.stopId = stopId;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public Integer getStopOrder() {
        return stopOrder;
    }

    public void setStopOrder(Integer stopOrder) {
        this.stopOrder = stopOrder;
    }
}
