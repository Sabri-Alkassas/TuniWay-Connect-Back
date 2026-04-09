package com.tuniway.connect.model.dto;

import java.util.List;

public class ClientTransportSearchResponse {
    private boolean success;
    private String message;
    private List<ClientTransportDto> transports;
    private int page;
    private int size;
    private long totalItems;
    private int totalPages;
    private String sort;

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

    public List<ClientTransportDto> getTransports() {
        return transports;
    }

    public void setTransports(List<ClientTransportDto> transports) {
        this.transports = transports;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }
}
