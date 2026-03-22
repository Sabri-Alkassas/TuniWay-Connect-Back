package com.tuniway.connect.model.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "transport")
public class Transport {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String zone;

    @Column(nullable = false)
    private Boolean active;

    public Transport() {
    }

    public Transport(UUID id, String name, String type, String zone, Boolean active) {
        this.id = id;
        this.name = name;
        this.type = type;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
