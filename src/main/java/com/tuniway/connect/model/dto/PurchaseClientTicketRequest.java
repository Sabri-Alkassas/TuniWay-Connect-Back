package com.tuniway.connect.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.time.LocalTime;
import java.util.UUID;

public class PurchaseClientTicketRequest {
    private UUID productId;
    private UUID transportId;
    private UUID fromStopId;
    private UUID toStopId;
    private LocalTime plannedDepartureTime;
    @JsonAlias("paymentProvider")
    private String provider;
    private String providerReference;
    private String paymentMethod;

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
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

    public LocalTime getPlannedDepartureTime() {
        return plannedDepartureTime;
    }

    public void setPlannedDepartureTime(LocalTime plannedDepartureTime) {
        this.plannedDepartureTime = plannedDepartureTime;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProviderReference() {
        return providerReference;
    }

    public void setProviderReference(String providerReference) {
        this.providerReference = providerReference;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
