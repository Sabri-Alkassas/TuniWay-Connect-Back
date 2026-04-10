package com.tuniway.connect.model.dto;

import java.util.List;

public class ClientTicketProductsResponse {
    private boolean success;
    private String message;
    private List<ClientTicketProductDto> products;

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

    public List<ClientTicketProductDto> getProducts() {
        return products;
    }

    public void setProducts(List<ClientTicketProductDto> products) {
        this.products = products;
    }
}
