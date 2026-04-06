package com.tuniway.connect.repository;

import com.tuniway.connect.model.entity.TransportDepartureSlot;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransportDepartureSlotRepository extends JpaRepository<TransportDepartureSlot, UUID> {
    @EntityGraph(attributePaths = {"stop"})
    List<TransportDepartureSlot> findByTransportIdOrderByStopOrderAscDayOfWeekAscDepartureTimeAsc(UUID transportId);

    void deleteByTransportId(UUID transportId);
}
