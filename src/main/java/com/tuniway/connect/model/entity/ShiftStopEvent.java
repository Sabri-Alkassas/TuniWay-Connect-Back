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
@Table(name = "shift_stop_events")
public class ShiftStopEvent {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workshift_id", nullable = false)
    private WorkShift workShift;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stop_id", nullable = false)
    private TransportStop stop;

    @Column(name = "stop_order", nullable = false)
    private Integer stopOrder;

    @Column(name = "expected_departure_time", nullable = false)
    private Instant expectedDepartureTime;

    @Column(name = "arrived_at")
    private Instant arrivedAt;

    @Column(name = "departed_at")
    private Instant departedAt;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public WorkShift getWorkShift() {
        return workShift;
    }

    public void setWorkShift(WorkShift workShift) {
        this.workShift = workShift;
    }

    public TransportStop getStop() {
        return stop;
    }

    public void setStop(TransportStop stop) {
        this.stop = stop;
    }

    public Integer getStopOrder() {
        return stopOrder;
    }

    public void setStopOrder(Integer stopOrder) {
        this.stopOrder = stopOrder;
    }

    public Instant getExpectedDepartureTime() {
        return expectedDepartureTime;
    }

    public void setExpectedDepartureTime(Instant expectedDepartureTime) {
        this.expectedDepartureTime = expectedDepartureTime;
    }

    public Instant getArrivedAt() {
        return arrivedAt;
    }

    public void setArrivedAt(Instant arrivedAt) {
        this.arrivedAt = arrivedAt;
    }

    public Instant getDepartedAt() {
        return departedAt;
    }

    public void setDepartedAt(Instant departedAt) {
        this.departedAt = departedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
