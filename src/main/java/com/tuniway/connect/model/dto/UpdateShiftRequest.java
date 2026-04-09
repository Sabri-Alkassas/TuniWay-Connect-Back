package com.tuniway.connect.model.dto;

import java.time.Instant;
import java.util.UUID;

public class UpdateShiftRequest {
    private Instant newStart;
    private Instant newEnd;
    private UUID transportId;

    public Instant getNewStart() {
        return newStart;
    }

    public void setNewStart(Instant newStart) {
        this.newStart = newStart;
    }

    public Instant getNewEnd() {
        return newEnd;
    }

    public void setNewEnd(Instant newEnd) {
        this.newEnd = newEnd;
    }

    public UUID getTransportId() {
        return transportId;
    }

    public void setTransportId(UUID transportId) {
        this.transportId = transportId;
    }
}
