package com.tuniway.connect.model.dto;

import com.tuniway.connect.model.entity.TransportType;

public class UpdateTransportRequest {
    public String name;
    public String code;
    public TransportType transportType;

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public TransportType getTransportType() {
        return transportType;
    }

    public void setTransportType(TransportType transportType) {
        this.transportType = transportType;
    }
}
