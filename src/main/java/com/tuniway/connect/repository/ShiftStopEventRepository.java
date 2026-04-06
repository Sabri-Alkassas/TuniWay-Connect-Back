package com.tuniway.connect.repository;

import com.tuniway.connect.model.entity.ShiftStopEvent;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShiftStopEventRepository extends JpaRepository<ShiftStopEvent, UUID> {
    @EntityGraph(attributePaths = {"stop"})
    List<ShiftStopEvent> findByWorkShiftIdOrderByStopOrderAsc(UUID workShiftId);

    @EntityGraph(attributePaths = {"stop", "workShift"})
    Optional<ShiftStopEvent> findByWorkShiftIdAndStopId(UUID workShiftId, UUID stopId);

    void deleteByWorkShiftId(UUID workShiftId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = """
            UPDATE shift_stop_events
            SET arrived_at = :now,
            status = 'arrived',
            updated_at = :now
            WHERE workshift_id = :shiftId
              AND stop_id = :stopId
              AND (status = 'pending' OR status IS NULL)
            """,
        nativeQuery = true
    )
    int markArrivedIfPending(
        @Param("shiftId") UUID shiftId,
        @Param("stopId") UUID stopId,
        @Param("now") Instant now
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = """
            UPDATE shift_stop_events
            SET departed_at = :now,
            status = 'departed',
            updated_at = :now
            WHERE workshift_id = :shiftId
              AND stop_id = :stopId
              AND status = 'arrived'
            """,
        nativeQuery = true
    )
    int markDepartedIfArrived(
        @Param("shiftId") UUID shiftId,
        @Param("stopId") UUID stopId,
        @Param("now") Instant now
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = """
            UPDATE shift_stop_events
            SET arrived_at = COALESCE(arrived_at, :now),
            departed_at = :now,
            status = 'departed',
            updated_at = :now
            WHERE workshift_id = :shiftId
              AND stop_id = :stopId
              AND (status = 'pending' OR status IS NULL)
            """,
        nativeQuery = true
    )
    int markDepartedDirectlyIfPending(
        @Param("shiftId") UUID shiftId,
        @Param("stopId") UUID stopId,
        @Param("now") Instant now
    );
}
