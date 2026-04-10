package com.tuniway.connect.model.dto;

import java.util.List;
import java.util.UUID;

public class ClientTransportStopsResponse {
    private boolean success;
    private String message;
    private UUID transportId;
    private String transportName;
    private List<ClientTransportStopDto> stops;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UUID getTransportId() {
        return transportId;
    }

    public void setTransportId(UUID transportId) {
        this.transportId = transportId;
    }

    public String getTransportName() {
        return transportName;
    }

    public void setTransportName(String transportName) {
        this.transportName = transportName;
    }

    public List<ClientTransportStopDto> getStops() {
        return stops;
    }

    public void setStops(List<ClientTransportStopDto> stops) {
        this.stops = stops;
    }
}
