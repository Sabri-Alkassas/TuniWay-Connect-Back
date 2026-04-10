package com.tuniway.connect.model.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class ClientTicketProductDto {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer validDurationMinutes;
    private Boolean active;
    private UUID transportId;
    private UUID fromStopId;
    private UUID toStopId;
    private Integer stopCount;

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

    public UUID getTransportId() {
        return transportId;
    }

    public void setTransportId(UUID transportId) {
        this.transportId = transportId;
    }

    public UUID getFromStopId() {
        return fromStopId;
    }

    public void setFromStopId(UUID fromStopId) {
        this.fromStopId = fromStopId;
    }

    public UUID getToStopId() {
        return toStopId;
    }

    public void setToStopId(UUID toStopId) {
        this.toStopId = toStopId;
    }

    public Integer getStopCount() {
        return stopCount;
    }

    public void setStopCount(Integer stopCount) {
        this.stopCount = stopCount;
    }
}
