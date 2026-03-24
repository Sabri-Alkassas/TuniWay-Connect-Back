package com.tuniway.connect.model.dto;

import java.time.Instant;
import java.util.List;

public class EmployeeShiftProgressResponse {

    private String message;
    private String shiftId;
    private String shiftStatus;
    private ProgressStopDto currentStop;
    private List<ProgressStopDto> completedStops;
    private Integer completedStopsCount;
    private Integer totalStops;
    private ProgressStopDto nextStop;
    private Long delayMinutes;

    public static class ProgressStopDto {
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

    public String getShiftStatus() {
        return shiftStatus;
    }

    public void setShiftStatus(String shiftStatus) {
        this.shiftStatus = shiftStatus;
    }

    public ProgressStopDto getCurrentStop() {
        return currentStop;
    }

    public void setCurrentStop(ProgressStopDto currentStop) {
        this.currentStop = currentStop;
    }

    public List<ProgressStopDto> getCompletedStops() {
        return completedStops;
    }

    public void setCompletedStops(List<ProgressStopDto> completedStops) {
        this.completedStops = completedStops;
    }

    public Integer getCompletedStopsCount() {
        return completedStopsCount;
    }

    public void setCompletedStopsCount(Integer completedStopsCount) {
        this.completedStopsCount = completedStopsCount;
    }

    public Integer getTotalStops() {
        return totalStops;
    }

    public void setTotalStops(Integer totalStops) {
        this.totalStops = totalStops;
    }

    public ProgressStopDto getNextStop() {
        return nextStop;
    }

    public void setNextStop(ProgressStopDto nextStop) {
        this.nextStop = nextStop;
    }

    public Long getDelayMinutes() {
        return delayMinutes;
    }

    public void setDelayMinutes(Long delayMinutes) {
        this.delayMinutes = delayMinutes;
    }
}
