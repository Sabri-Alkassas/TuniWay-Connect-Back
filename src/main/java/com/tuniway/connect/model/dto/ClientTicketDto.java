package com.tuniway.connect.model.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class ClientTicketDto {
    private UUID ticketId;
    private UUID productId;
    private String productName;
    private String productDescription;
    private UUID transportId;
    private String transportCode;
    private String transportName;
    private String transportType;
    private UUID fromStopId;
    private String fromStopName;
    private UUID toStopId;
    private String toStopName;
    private Integer stopCount;
    private BigDecimal price;
    private Integer validDurationMinutes;
    private String status;
    private Instant validUntil;
    private Instant purchaseTime;
    private ClientTicketPaymentDto payment;

    public UUID getTicketId() {
        return ticketId;
    }

    public void setTicketId(UUID ticketId) {
        this.ticketId = ticketId;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public UUID getTransportId() {
        return transportId;
    }

    public void setTransportId(UUID transportId) {
        this.transportId = transportId;
    }

    public String getTransportCode() {
        return transportCode;
    }

    public void setTransportCode(String transportCode) {
        this.transportCode = transportCode;
    }

    public String getTransportName() {
        return transportName;
    }

    public void setTransportName(String transportName) {
        this.transportName = transportName;
    }

    public String getTransportType() {
        return transportType;
    }

    public void setTransportType(String transportType) {
        this.transportType = transportType;
    }

    public UUID getFromStopId() {
        return fromStopId;
    }

    public void setFromStopId(UUID fromStopId) {
        this.fromStopId = fromStopId;
    }

    public String getFromStopName() {
        return fromStopName;
    }

    public void setFromStopName(String fromStopName) {
        this.fromStopName = fromStopName;
    }

    public UUID getToStopId() {
        return toStopId;
    }

    public void setToStopId(UUID toStopId) {
        this.toStopId = toStopId;
    }

    public String getToStopName() {
        return toStopName;
    }

    public void setToStopName(String toStopName) {
        this.toStopName = toStopName;
    }

    public Integer getStopCount() {
        return stopCount;
    }

    public void setStopCount(Integer stopCount) {
        this.stopCount = stopCount;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Instant validUntil) {
        this.validUntil = validUntil;
    }

    public Instant getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(Instant purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

    public ClientTicketPaymentDto getPayment() {
        return payment;
    }

    public void setPayment(ClientTicketPaymentDto payment) {
        this.payment = payment;
    }
}
