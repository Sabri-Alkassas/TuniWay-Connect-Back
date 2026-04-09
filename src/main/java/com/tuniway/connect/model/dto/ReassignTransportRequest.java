package com.tuniway.connect.model.dto;

import java.util.UUID;

public class ReassignTransportRequest {
    private UUID newTransportId;

    public UUID getNewTransportId() {
        return newTransportId;
    }

    public void setNewTransportId(UUID newTransportId) {
        this.newTransportId = newTransportId;
    }
}
