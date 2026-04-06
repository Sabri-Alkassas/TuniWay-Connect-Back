package com.tuniway.connect.repository;

import com.tuniway.connect.model.entity.TransportStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransportStopRepository extends JpaRepository<TransportStop, UUID> {
}
