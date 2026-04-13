package com.tuniway.connect.repository;

import com.tuniway.connect.model.entity.WorkShift;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkShiftRepository extends JpaRepository<WorkShift, UUID> {
    @EntityGraph(attributePaths = {"transport"})
    List<WorkShift> findAllByOrderByScheduleStartDesc();

    @EntityGraph(attributePaths = {"transport"})
    List<WorkShift> findByEmployeeIdOrderByScheduleStartAsc(UUID employeeId);

    @EntityGraph(attributePaths = {"transport"})
    Optional<WorkShift> findByIdAndEmployeeId(UUID id, UUID employeeId);
    long countByStatusIgnoreCase(String status);
    List<WorkShift> findByTransportId(UUID transportId);

    @Query("""
        select ws from WorkShift ws
        join fetch ws.transport t
        where t.id in :transportIds
          and lower(coalesce(ws.status, '')) = lower(:status)
          and ws.currentLatitude is not null
          and ws.currentLongitude is not null
          and ws.currentLocationUpdatedAt is not null
        order by ws.currentLocationUpdatedAt desc, ws.actualStart desc
        """)
    List<WorkShift> findLocatedByTransportIdsAndStatus(@Param("transportIds") Collection<UUID> transportIds,
                                                       @Param("status") String status);

    boolean existsByEmployeeIdAndIdNotAndScheduleStartLessThanAndScheduleEndGreaterThan(
        UUID employeeId,
        UUID id,
        Instant scheduleEnd,
        Instant scheduleStart
    );
}
