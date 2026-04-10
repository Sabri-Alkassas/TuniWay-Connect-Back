package com.tuniway.connect.repository;

import java.util.UUID;

public interface TransportCountProjection {
    UUID getTransportId();

    long getCount();
}