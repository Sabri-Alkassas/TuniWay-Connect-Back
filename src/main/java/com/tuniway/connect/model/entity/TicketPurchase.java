package com.tuniway.connect.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ticket_purchases")
public class TicketPurchase {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private TicketProduct product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transport_id")
    private Transport transport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_stop_id")
    private TransportStop fromStop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_stop_id")
    private TransportStop toStop;

    @Column(name = "stop_count")
    private Integer stopCount;

    @Column(name = "from_stop_order")
    private Integer fromStopOrder;

    @Column(name = "to_stop_order")
    private Integer toStopOrder;

    @Column(nullable = false)
    private String status;

    @Column(name = "valid_until", nullable = false)
    private Instant validUntil;

    @Column(name = "purchase_time", nullable = false)
    private Instant purchaseTime;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public TicketProduct getProduct() {
        return product;
    }

    public void setProduct(TicketProduct product) {
        this.product = product;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Transport getTransport() {
        return transport;
    }

    public void setTransport(Transport transport) {
        this.transport = transport;
    }

    public TransportStop getFromStop() {
        return fromStop;
    }

    public void setFromStop(TransportStop fromStop) {
        this.fromStop = fromStop;
    }

    public TransportStop getToStop() {
        return toStop;
    }

    public void setToStop(TransportStop toStop) {
        this.toStop = toStop;
    }

    public Integer getStopCount() {
        return stopCount;
    }

    public void setStopCount(Integer stopCount) {
        this.stopCount = stopCount;
    }

    public Integer getFromStopOrder() {
        return fromStopOrder;
    }

    public void setFromStopOrder(Integer fromStopOrder) {
        this.fromStopOrder = fromStopOrder;
    }

    public Integer getToStopOrder() {
        return toStopOrder;
    }

    public void setToStopOrder(Integer toStopOrder) {
        this.toStopOrder = toStopOrder;
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
}
