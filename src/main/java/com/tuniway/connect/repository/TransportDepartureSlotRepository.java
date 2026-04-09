package com.tuniway.connect.repository;

import com.tuniway.connect.model.entity.TransportDepartureSlot;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransportDepartureSlotRepository extends JpaRepository<TransportDepartureSlot, UUID> {
    @EntityGraph(attributePaths = {"stop"})
    List<TransportDepartureSlot> findByTransportIdOrderByStopOrderAscDayOfWeekAscDepartureTimeAsc(UUID transportId);

    @EntityGraph(attributePaths = {"stop"})
    List<TransportDepartureSlot> findByTransportIdAndDayOfWeekIgnoreCaseOrderByStopOrderAscDepartureTimeAsc(UUID transportId,
                                                                                                             String dayOfWeek);

        @Query("""
                select slot.transport.id as transportId, count(slot) as count
                from TransportDepartureSlot slot
                join slot.stop s
                where slot.transport.id in :transportIds
                    and slot.active = true
                    and s.active = true
                group by slot.transport.id
                """)
        List<TransportCountProjection> countVisibleDeparturesByTransportIds(@Param("transportIds") Collection<UUID> transportIds);

    void deleteByTransportId(UUID transportId);
}
