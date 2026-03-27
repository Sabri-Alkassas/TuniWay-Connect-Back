package com.tuniway.connect.model.dto;

import com.tuniway.connect.model.entity.TransportType;

public class CreateTransportRequest {
    private String code;
    private String name;
    private String route_name;
    private TransportType transportType;
    private String start_point;
    private String end_point;
    private String operating_zone;
    private String zone;
    private Boolean is_active;

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

    public String getRouteName() {
        return route_name;
    }

    public void setRouteName(String route_name) {
        this.route_name = route_name;
    }

    public String getStart_point() {
        return start_point;
    }

    public void setStart_point(String start_point) {
        this.start_point = start_point;
    }

    public String getEnd_point() {
        return end_point;
    }

    public void setEnd_point(String end_point) {
        this.end_point = end_point;
    }

    public String getOperating_zone() {
        return operating_zone;
    }

    public void setOperating_zone(String operating_zone) {
        this.operating_zone = operating_zone;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public Boolean getIs_active() {
        return is_active;
    }

    public void setIs_active(Boolean is_active) {
        this.is_active = is_active;
    }

    public TransportType getTransportType() {
        return transportType;
    }

    public void setTransportType(TransportType transportType) {
        this.transportType = transportType;
    }
}
