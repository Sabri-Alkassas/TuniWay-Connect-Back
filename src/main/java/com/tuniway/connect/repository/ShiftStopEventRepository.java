package com.tuniway.connect.repository;

import com.tuniway.connect.model.entity.ShiftStopEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShiftStopEventRepository extends JpaRepository<ShiftStopEvent, UUID> {
    List<ShiftStopEvent> findByWorkShiftIdOrderByStopOrderAsc(UUID workShiftId);

    Optional<ShiftStopEvent> findByWorkShiftIdAndStopId(UUID workShiftId, UUID stopId);
}
