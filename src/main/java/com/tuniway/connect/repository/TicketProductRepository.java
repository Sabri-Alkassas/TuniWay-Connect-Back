package com.tuniway.connect.repository;

import com.tuniway.connect.model.entity.TicketProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketProductRepository extends JpaRepository<TicketProduct, UUID> {
    List<TicketProduct> findByActiveTrueOrderByPriceAscValidDurationMinutesAscNameAsc();

    Optional<TicketProduct> findByIdAndActiveTrue(UUID id);
}
