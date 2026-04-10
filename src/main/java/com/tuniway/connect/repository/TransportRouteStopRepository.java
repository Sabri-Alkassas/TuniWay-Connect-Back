package com.tuniway.connect.repository;

import com.tuniway.connect.model.entity.TransportRouteStop;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransportRouteStopRepository extends JpaRepository<TransportRouteStop, UUID> {
    @EntityGraph(attributePaths = {"stop"})
    List<TransportRouteStop> findByTransportIdOrderByStopOrderAsc(UUID transportId);

        @Query("""
                select trs.transport.id as transportId, count(trs) as count
                from TransportRouteStop trs
                join trs.stop s
                where trs.transport.id in :transportIds
                    and trs.active = true
                    and s.active = true
                group by trs.transport.id
                """)
        List<TransportCountProjection> countVisibleStopsByTransportIds(@Param("transportIds") Collection<UUID> transportIds);

        @EntityGraph(attributePaths = {"transport", "stop"})
        @Query("""
                select distinct trs
                from TransportRouteStop trs
                join trs.transport t
                join trs.stop s
                where t.active = true
                    and trs.active = true
                    and s.active = true
                    and s.latitude is not null
                    and s.longitude is not null
                    and s.latitude between :minLatitude and :maxLatitude
                    and s.longitude between :minLongitude and :maxLongitude
                """)
        List<TransportRouteStop> findEligibleNearbyRouteStops(@Param("minLatitude") double minLatitude,
                                                                                                                    @Param("maxLatitude") double maxLatitude,
                                                                                                                    @Param("minLongitude") double minLongitude,
                                                                                                                    @Param("maxLongitude") double maxLongitude);

    void deleteByTransportId(UUID transportId);
}
