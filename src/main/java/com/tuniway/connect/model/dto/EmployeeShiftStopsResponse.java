package com.tuniway.connect.model.dto;

import java.time.Instant;
import java.util.List;

public class EmployeeShiftStopsResponse {
    private String message;
    private String shiftId;
    private List<ShiftStopDto> stops;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getShiftId() {
        return shiftId;
    }

    public void setShiftId(String shiftId) {
        this.shiftId = shiftId;
    }

    public List<ShiftStopDto> getStops() {
        return stops;
    }

    public void setStops(List<ShiftStopDto> stops) {
        this.stops = stops;
    }

    public static class ShiftStopDto {
        private String stopId;
        private Integer stopOrder;
        private String stopName;
        private String status;
        private Instant expectedDepartureTime;
        private Instant arrivedAt;
        private Instant departedAt;

        public String getStopId() {
            return stopId;
        }

        public void setStopId(String stopId) {
            this.stopId = stopId;
        }

        public Integer getStopOrder() {
            return stopOrder;
        }

        public void setStopOrder(Integer stopOrder) {
            this.stopOrder = stopOrder;
        }

        public String getStopName() {
            return stopName;
        }

        public void setStopName(String stopName) {
            this.stopName = stopName;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Instant getExpectedDepartureTime() {
            return expectedDepartureTime;
        }

        public void setExpectedDepartureTime(Instant expectedDepartureTime) {
            this.expectedDepartureTime = expectedDepartureTime;
        }

        public Instant getArrivedAt() {
            return arrivedAt;
        }

        public void setArrivedAt(Instant arrivedAt) {
            this.arrivedAt = arrivedAt;
        }

        public Instant getDepartedAt() {
            return departedAt;
        }

        public void setDepartedAt(Instant departedAt) {
            this.departedAt = departedAt;
        }
    }
}
