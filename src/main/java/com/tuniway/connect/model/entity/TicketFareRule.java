package com.tuniway.connect.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "ticket_fare_rules")
public class TicketFareRule {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "transport_code")
    private String transportCode;

    @Column(name = "transport_type", nullable = false)
    private String transportType;

    @Column(name = "fare_class", nullable = false)
    private String fareClass;

    @Column(name = "min_sections", nullable = false)
    private Integer minSections;

    @Column(name = "max_sections", nullable = false)
    private Integer maxSections;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal price;

    @Column(nullable = false)
    private String currency;

    @Column
    private String notes;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTransportCode() {
        return transportCode;
    }

    public void setTransportCode(String transportCode) {
        this.transportCode = transportCode;
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }

    public String getFareClass() {
        return fareClass;
    }

    public void setFareClass(String fareClass) {
        this.fareClass = fareClass;
    }

    public Integer getMinSections() {
        return minSections;
    }

    public void setMinSections(Integer minSections) {
        this.minSections = minSections;
    }

    public Integer getMaxSections() {
        return maxSections;
    }

    public void setMaxSections(Integer maxSections) {
        this.maxSections = maxSections;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
