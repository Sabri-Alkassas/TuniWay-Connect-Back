package com.tuniway.connect.repository;

import com.tuniway.connect.model.entity.TicketFareRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TicketFareRuleRepository extends JpaRepository<TicketFareRule, UUID> {
    List<TicketFareRule> findByTransportCodeIgnoreCaseAndFareClassIgnoreCaseOrderByMinSectionsAsc(
        String transportCode,
        String fareClass
    );

    List<TicketFareRule> findByTransportCodeIsNullAndTransportTypeIgnoreCaseAndFareClassIgnoreCaseOrderByMinSectionsAsc(
        String transportType,
        String fareClass
    );
}
