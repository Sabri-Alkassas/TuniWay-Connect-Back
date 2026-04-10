package com.tuniway.connect.model.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ClientTransportDeparturesResponse {
    private boolean success;
    private String message;
    private UUID transportId;
    private String transportName;
    private LocalDate date;
    private String dayOfWeek;
    private List<ClientTransportDepartureDto> departures;

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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public List<ClientTransportDepartureDto> getDepartures() {
        return departures;
    }

    public void setDepartures(List<ClientTransportDepartureDto> departures) {
        this.departures = departures;
    }
}
