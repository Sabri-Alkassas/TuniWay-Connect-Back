package com.tuniway.connect.model.dto;

public class ClientTransportDetailsResponse {
    private boolean success;
    private String message;
    private ClientTransportDto transport;

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

    public ClientTransportDto getTransport() {
        return transport;
    }

    public void setTransport(ClientTransportDto transport) {
        this.transport = transport;
    }
}
