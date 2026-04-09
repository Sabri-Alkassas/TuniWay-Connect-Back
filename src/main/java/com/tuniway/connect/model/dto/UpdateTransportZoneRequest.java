package com.tuniway.connect.model.dto;

public class UpdateTransportZoneRequest {
    private String zone;
    private String operating_zone;

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getOperating_zone() {
        return operating_zone;
    }

    public void setOperating_zone(String operating_zone) {
        this.operating_zone = operating_zone;
    }
}
