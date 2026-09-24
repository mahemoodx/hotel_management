package com.royalpearl.hotel.order.repository;

import com.royalpearl.hotel.order.entity.TableReservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface TableReservationRepository extends JpaRepository<TableReservation, UUID> {

    Optional<TableReservation> findByOrderId(String orderId);

    boolean existsByOrderId(String orderId);

    /** Guest status lookup – reference + phone. */
    Optional<TableReservation> findByOrderIdAndPhone(String orderId, String phone);

    // ----------------------------------------------------------------
    // Authenticated user – own records only
    // ----------------------------------------------------------------
    Page<TableReservation> findAllByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    // ----------------------------------------------------------------
    // Admin search
    // ----------------------------------------------------------------
    @Query("""
            SELECT o FROM TableReservation o
            WHERE (:search IS NULL
                   OR LOWER(o.orderId) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(o.name)    LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(o.phone)   LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:status IS NULL        OR o.status        = :status)
              AND (:paymentStatus IS NULL OR o.paymentStatus = :paymentStatus)
              AND (:orderStatus IS NULL   OR o.orderStatus   = :orderStatus)
              AND (:from IS NULL          OR o.createdAt    >= :from)
              AND (:to   IS NULL          OR o.createdAt    <= :to)
            """)
    Page<TableReservation> adminSearch(
            @Param("search")        String search,
            @Param("status")        String status,
            @Param("paymentStatus") String paymentStatus,
            @Param("orderStatus")   String orderStatus,
            @Param("from")          OffsetDateTime from,
            @Param("to")            OffsetDateTime to,
            Pageable pageable
    );

    // ----------------------------------------------------------------
    // Stats
    // ----------------------------------------------------------------
    long countByPaymentStatus(String paymentStatus);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM TableReservation o WHERE o.paymentStatus = 'paid'")
    java.math.BigDecimal sumRevenue();
}
