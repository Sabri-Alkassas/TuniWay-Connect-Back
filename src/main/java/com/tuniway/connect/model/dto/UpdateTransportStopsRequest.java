package com.tuniway.connect.model.dto;

import java.util.List;

public class UpdateTransportStopsRequest {
    private List<TransportStopItem> stops;

    public List<TransportStopItem> getStops() {
        return stops;
    }

    public void setStops(List<TransportStopItem> stops) {
        this.stops = stops;
    }
}
