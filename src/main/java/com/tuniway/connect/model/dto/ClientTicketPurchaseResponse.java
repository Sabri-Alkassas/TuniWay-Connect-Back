package com.tuniway.connect.model.dto;

public class ClientTicketPurchaseResponse {
    private boolean success;
    private String message;
    private ClientTicketDto ticket;

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

    public ClientTicketDto getTicket() {
        return ticket;
    }

    public void setTicket(ClientTicketDto ticket) {
        this.ticket = ticket;
    }
}
