package com.tuniway.connect.repository;

import com.tuniway.connect.model.entity.TicketPurchase;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface TicketPurchaseRepository extends JpaRepository<TicketPurchase, UUID> {
    @EntityGraph(attributePaths = {"product", "transport", "fromStop", "toStop"})
    Page<TicketPurchase> findByUserId(UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = {"product", "transport", "fromStop", "toStop"})
    List<TicketPurchase> findTop3ByUserIdOrderByPurchaseTimeDesc(UUID userId);

    long countByUserId(UUID userId);

    long countByUserIdAndStatusIgnoreCaseAndValidUntilAfter(UUID userId, String status, Instant now);

    @Query("""
        select count(p)
        from TicketPurchase p
        where p.transport.id = :transportId
            and upper(p.status) = upper(:status)
            and p.validUntil > :now
            and p.fromStopOrder is not null
            and p.toStopOrder is not null
            and p.fromStopOrder < :toStopOrder
            and p.toStopOrder > :fromStopOrder
        """)
    long countActiveOverlappingSegments(@Param("transportId") UUID transportId,
                                        @Param("fromStopOrder") Integer fromStopOrder,
                                        @Param("toStopOrder") Integer toStopOrder,
                                        @Param("status") String status,
                                        @Param("now") Instant now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"product", "transport", "fromStop", "toStop"})
    @Query("select p from TicketPurchase p where p.id = :id")
    java.util.Optional<TicketPurchase> findByIdForUpdate(@Param("id") UUID id);
}
