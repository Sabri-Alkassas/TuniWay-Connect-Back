package com.tuniway.connect.repository;

import com.tuniway.connect.model.entity.TicketPurchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TicketPurchaseRepository extends JpaRepository<TicketPurchase, UUID> {
    @EntityGraph(attributePaths = {"product", "transport", "fromStop", "toStop"})
    Page<TicketPurchase> findByUserId(UUID userId, Pageable pageable);
}
