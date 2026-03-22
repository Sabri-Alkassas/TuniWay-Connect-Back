package com.tuniway.connect.model.dto;

import java.time.Instant;
import java.util.List;

public class EmployeeScheduleResponse {
    private String message;
    private List<ShiftDto> shifts;

    public EmployeeScheduleResponse() {
    }

    public EmployeeScheduleResponse(String message, List<ShiftDto> shifts) {
        this.message = message;
        this.shifts = shifts;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ShiftDto> getShifts() {
        return shifts;
    }

    public void setShifts(List<ShiftDto> shifts) {
        this.shifts = shifts;
    }

    public static class ShiftDto {
        private String shiftId;
        private String transportId;
        private String transportName;
        private String transportType;
        private String transportZone;
        private Instant scheduleStart;
        private Instant scheduleEnd;
        private String status;
        private Instant actualStart;
        private Instant actualEnd;

        public ShiftDto() {
        }

        public ShiftDto(String shiftId, String transportId, String transportName, String transportType,
                        String transportZone, Instant scheduleStart, Instant scheduleEnd, String status,
                        Instant actualStart, Instant actualEnd) {
            this.shiftId = shiftId;
            this.transportId = transportId;
            this.transportName = transportName;
            this.transportType = transportType;
            this.transportZone = transportZone;
            this.scheduleStart = scheduleStart;
            this.scheduleEnd = scheduleEnd;
            this.status = status;
            this.actualStart = actualStart;
            this.actualEnd = actualEnd;
        }

        public String getShiftId() {
            return shiftId;
        }

        public void setShiftId(String shiftId) {
            this.shiftId = shiftId;
        }

        public String getTransportId() {
            return transportId;
        }

        public void setTransportId(String transportId) {
            this.transportId = transportId;
        }

        public String getTransportName() {
            return transportName;
        }

        public void setTransportName(String transportName) {
            this.transportName = transportName;
        }

        public String getTransportType() {
            return transportType;
        }

        public void setTransportType(String transportType) {
            this.transportType = transportType;
        }

        public String getTransportZone() {
            return transportZone;
        }

        public void setTransportZone(String transportZone) {
            this.transportZone = transportZone;
        }

        public Instant getScheduleStart() {
            return scheduleStart;
        }

        public void setScheduleStart(Instant scheduleStart) {
            this.scheduleStart = scheduleStart;
        }

        public Instant getScheduleEnd() {
            return scheduleEnd;
        }

        public void setScheduleEnd(Instant scheduleEnd) {
            this.scheduleEnd = scheduleEnd;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Instant getActualStart() {
            return actualStart;
        }

        public void setActualStart(Instant actualStart) {
            this.actualStart = actualStart;
        }

        public Instant getActualEnd() {
            return actualEnd;
        }

        public void setActualEnd(Instant actualEnd) {
            this.actualEnd = actualEnd;
        }
    }
}
