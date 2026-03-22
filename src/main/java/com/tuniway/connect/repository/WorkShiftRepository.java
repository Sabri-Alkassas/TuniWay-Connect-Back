package com.tuniway.connect.repository;

import com.tuniway.connect.model.entity.WorkShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkShiftRepository extends JpaRepository<WorkShift, UUID> {
    List<WorkShift> findByEmployeeIdOrderByScheduleStartAsc(UUID employeeId);
}
