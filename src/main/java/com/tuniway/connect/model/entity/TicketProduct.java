package com.tuniway.connect.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "ticket_products")
public class TicketProduct {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "product_code")
    private String productCode;

    @Column(name = "valid_duration_minutes", nullable = false)
    private Integer validDurationMinutes;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "fare_class", nullable = false)
    private String fareClass;

    @Column(name = "allowed_transport_code")
    private String allowedTransportCode;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getValidDurationMinutes() {
        return validDurationMinutes;
    }

    public void setValidDurationMinutes(Integer validDurationMinutes) {
        this.validDurationMinutes = validDurationMinutes;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getFareClass() {
        return fareClass;
    }

    public void setFareClass(String fareClass) {
        this.fareClass = fareClass;
    }

    public String getAllowedTransportCode() {
        return allowedTransportCode;
    }

    public void setAllowedTransportCode(String allowedTransportCode) {
        this.allowedTransportCode = allowedTransportCode;
    }
}
