package com.tuniway.connect.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tuniway.connect.model.entity.Transport;

@Repository
public interface TransportRepository extends JpaRepository<Transport, UUID> {
    Optional<Transport> findByCode(String code);
}
