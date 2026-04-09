package com.tuniway.connect.model.dto;

import java.util.List;

public class UpdateTransportDeparturesRequest {
    private List<TransportDepartureItem> departures;

    public List<TransportDepartureItem> getDepartures() {
        return departures;
    }

    public void setDepartures(List<TransportDepartureItem> departures) {
        this.departures = departures;
    }
}
