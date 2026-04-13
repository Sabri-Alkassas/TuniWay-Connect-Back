package com.tuniway.connect.model.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workshifts")
public class WorkShift {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transport_id", nullable = false)
    private Transport transport;

    @Column(name = "schedule_start", nullable = false)
    private Instant scheduleStart;

    @Column(name = "schedule_end", nullable = false)
    private Instant scheduleEnd;

    @Column(name = "status")
    private String status;

    @Column(name = "actual_start")
    private Instant actualStart;

    @Column(name = "actual_end")
    private Instant actualEnd;

    @Column(name = "current_latitude", precision = 9, scale = 6)
    private BigDecimal currentLatitude;

    @Column(name = "current_longitude", precision = 9, scale = 6)
    private BigDecimal currentLongitude;

    @Column(name = "current_location_updated_at")
    private Instant currentLocationUpdatedAt;

    public WorkShift() {
    }

    public WorkShift(UUID id, UUID employeeId, Transport transport, Instant scheduleStart, 
                     Instant scheduleEnd, String status, Instant actualStart, Instant actualEnd) {
        this.id = id;
        this.employeeId = employeeId;
        this.transport = transport;
        this.scheduleStart = scheduleStart;
        this.scheduleEnd = scheduleEnd;
        this.status = status;
        this.actualStart = actualStart;
        this.actualEnd = actualEnd;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(UUID employeeId) {
        this.employeeId = employeeId;
    }

    public Transport getTransport() {
        return transport;
    }

    public void setTransport(Transport transport) {
        this.transport = transport;
    }

    public Instant getScheduleStart() {
        return scheduleStart;
    }

    public void setScheduleStart(Instant scheduleStart) {
        this.scheduleStart = scheduleStart;
    }

    public Instant getScheduleEnd() {
        return scheduleEnd;
    }

    public void setScheduleEnd(Instant scheduleEnd) {
        this.scheduleEnd = scheduleEnd;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getActualStart() {
        return actualStart;
    }

    public void setActualStart(Instant actualStart) {
        this.actualStart = actualStart;
    }

    public Instant getActualEnd() {
        return actualEnd;
    }

    public void setActualEnd(Instant actualEnd) {
        this.actualEnd = actualEnd;
    }

    public BigDecimal getCurrentLatitude() {
        return currentLatitude;
    }

    public void setCurrentLatitude(BigDecimal currentLatitude) {
        this.currentLatitude = currentLatitude;
    }

    public BigDecimal getCurrentLongitude() {
        return currentLongitude;
    }

    public void setCurrentLongitude(BigDecimal currentLongitude) {
        this.currentLongitude = currentLongitude;
    }

    public Instant getCurrentLocationUpdatedAt() {
        return currentLocationUpdatedAt;
    }

    public void setCurrentLocationUpdatedAt(Instant currentLocationUpdatedAt) {
        this.currentLocationUpdatedAt = currentLocationUpdatedAt;
    }
}
