package com.tuniway.connect.repository;

import com.tuniway.connect.model.entity.WorkShift;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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

    Optional<WorkShift> findByIdAndEmployeeId(UUID id, UUID employeeId);
    long countByStatusIgnoreCase(String status);
    List<WorkShift> findByTransportId(UUID transportId);
    boolean existsByEmployeeIdAndIdNotAndScheduleStartLessThanAndScheduleEndGreaterThan(
        UUID employeeId,
        UUID id,
        Instant scheduleEnd,
        Instant scheduleStart
    );
}
