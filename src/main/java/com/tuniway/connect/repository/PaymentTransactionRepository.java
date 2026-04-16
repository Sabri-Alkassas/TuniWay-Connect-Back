package com.tuniway.connect.repository;

import com.tuniway.connect.model.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {
    @EntityGraph(attributePaths = {"ticketPurchase"})
    List<PaymentTransaction> findByTicketPurchaseIdInOrderByProcessedAtDesc(List<UUID> ticketPurchaseIds);

    Optional<PaymentTransaction> findFirstByTicketPurchaseIdOrderByProcessedAtDesc(UUID ticketPurchaseId);
}
