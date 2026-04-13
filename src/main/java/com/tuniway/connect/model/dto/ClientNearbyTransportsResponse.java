package com.tuniway.connect.model.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class ClientNearbyTransportsResponse {
    private boolean success;
    private String message;
    private double latitude;
    private double longitude;
    private int radiusMeters;
    private List<NearbyTransportDto> transports;

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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getRadiusMeters() {
        return radiusMeters;
    }

    public void setRadiusMeters(int radiusMeters) {
        this.radiusMeters = radiusMeters;
    }

    public List<NearbyTransportDto> getTransports() {
        return transports;
    }

    public void setTransports(List<NearbyTransportDto> transports) {
        this.transports = transports;
    }

    public static class NearbyTransportDto {
        private ClientTransportDto transport;
        private ClientTransportStopDto nearestStop;
        private double distanceMeters;
        private int matchingStopCount;
        private BigDecimal markerLatitude;
        private BigDecimal markerLongitude;
        private Instant locationUpdatedAt;
        private String locationSource;

        public ClientTransportDto getTransport() {
            return transport;
        }

        public void setTransport(ClientTransportDto transport) {
            this.transport = transport;
        }

        public ClientTransportStopDto getNearestStop() {
            return nearestStop;
        }

        public void setNearestStop(ClientTransportStopDto nearestStop) {
            this.nearestStop = nearestStop;
        }

        public double getDistanceMeters() {
            return distanceMeters;
        }

        public void setDistanceMeters(double distanceMeters) {
            this.distanceMeters = distanceMeters;
        }

        public int getMatchingStopCount() {
            return matchingStopCount;
        }

        public void setMatchingStopCount(int matchingStopCount) {
            this.matchingStopCount = matchingStopCount;
        }

        public BigDecimal getMarkerLatitude() {
            return markerLatitude;
        }

        public void setMarkerLatitude(BigDecimal markerLatitude) {
            this.markerLatitude = markerLatitude;
        }

        public BigDecimal getMarkerLongitude() {
            return markerLongitude;
        }

        public void setMarkerLongitude(BigDecimal markerLongitude) {
            this.markerLongitude = markerLongitude;
        }

        public Instant getLocationUpdatedAt() {
            return locationUpdatedAt;
        }

        public void setLocationUpdatedAt(Instant locationUpdatedAt) {
            this.locationUpdatedAt = locationUpdatedAt;
        }

        public String getLocationSource() {
            return locationSource;
        }

        public void setLocationSource(String locationSource) {
            this.locationSource = locationSource;
        }
    }
}
