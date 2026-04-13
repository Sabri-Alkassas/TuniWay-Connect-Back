package com.tuniway.connect.repository;

import com.tuniway.connect.model.entity.TransportFareProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransportFareProfileRepository extends JpaRepository<TransportFareProfile, String> {
    Optional<TransportFareProfile> findByTransportCodeIgnoreCase(String transportCode);
}
