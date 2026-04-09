package com.tuniway.connect.model.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class PlanningPublishRequest {
    private List<PlanningChange> changes;

    public List<PlanningChange> getChanges() {
        return changes;
    }

    public void setChanges(List<PlanningChange> changes) {
        this.changes = changes;
    }

    public static class PlanningChange {
        private UUID shiftId;
        private Instant newStart;
        private Instant newEnd;
        private UUID transportId;

        public UUID getShiftId() {
            return shiftId;
        }

        public void setShiftId(UUID shiftId) {
            this.shiftId = shiftId;
        }

        public Instant getNewStart() {
            return newStart;
        }

        public void setNewStart(Instant newStart) {
            this.newStart = newStart;
        }

        public Instant getNewEnd() {
            return newEnd;
        }

        public void setNewEnd(Instant newEnd) {
            this.newEnd = newEnd;
        }

        public UUID getTransportId() {
            return transportId;
        }

        public void setTransportId(UUID transportId) {
            this.transportId = transportId;
        }
    }
}
