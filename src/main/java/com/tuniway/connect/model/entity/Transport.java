package com.tuniway.connect.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "transport")
public class Transport {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Convert(converter = TransportTypeConverter.class)
    private TransportType type;

    @Column(nullable = false)
    private String route_name;

    @Column(nullable = false)
    private String start_point;

    @Column(nullable = false)
    private String end_point;

    @Column(nullable = false)
    private String operating_zone;

    @Column(nullable = false)
    private String zone;

    @Column(nullable = false)
    private Boolean active;

    public Transport() {
    }

    public Transport(String code, String name, TransportType type, String route_name, String start_point, String end_point, String operating_zone, String zone, Boolean active) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.route_name = route_name;
        this.start_point = start_point;
        this.end_point = end_point;
        this.operating_zone = operating_zone;
        this.zone = zone;
        this.active = active;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRoute_name() {
        return route_name;
    }

    public void setRoute_name(String route_name) {
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
