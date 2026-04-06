package com.tuniway.connect.repository;

import com.tuniway.connect.model.entity.TransportRouteStop;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransportRouteStopRepository extends JpaRepository<TransportRouteStop, UUID> {
    @EntityGraph(attributePaths = {"stop"})
    List<TransportRouteStop> findByTransportIdOrderByStopOrderAsc(UUID transportId);

    void deleteByTransportId(UUID transportId);
}
