package com.tuniway.connect.repository;

import com.tuniway.connect.model.entity.Transport;
import com.tuniway.connect.model.entity.TransportType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransportRepository extends JpaRepository<Transport, UUID> {
    Optional<Transport> findByCode(String code);
    long countByActiveTrue();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Transport t where t.id = :transportId")
    Optional<Transport> findByIdForUpdate(@Param("transportId") UUID transportId);

    @Query("""
        select t from Transport t
        where (:q is null
            or lower(t.code) like concat('%', :q, '%')
            or lower(t.name) like concat('%', :q, '%')
            or lower(t.route_name) like concat('%', :q, '%')
            or lower(t.start_point) like concat('%', :q, '%')
            or lower(t.end_point) like concat('%', :q, '%')
            or lower(t.zone) like concat('%', :q, '%')
            or lower(t.operating_zone) like concat('%', :q, '%'))
        and (:zone is null
            or lower(t.zone) = :zone
            or lower(t.operating_zone) = :zone)
        and (:type is null or t.type = :type)
        and (:active is null or t.active = :active)
        """)
    Page<Transport> searchForClient(@Param("q") String q,
                                    @Param("zone") String zone,
                                    @Param("type") TransportType type,
                                    @Param("active") Boolean active,
                                    Pageable pageable);
}
