package com.royalpearl.hotel.booking.repository;

import com.royalpearl.hotel.booking.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Optional<Booking> findByBookingId(String bookingId);

    boolean existsByBookingId(String bookingId);

    /** Guest status lookup – reference + phone. */
    Optional<Booking> findByBookingIdAndPhone(String bookingId, String phone);

    // ----------------------------------------------------------------
    // Authenticated user – own records only
    // ----------------------------------------------------------------
    Page<Booking> findAllByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    // ----------------------------------------------------------------
    // Admin search
    // ----------------------------------------------------------------
    @Query("""
            SELECT b FROM Booking b
            WHERE (:search IS NULL
                   OR LOWER(b.bookingId) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(b.fullName)  LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(b.email)     LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(b.phone)     LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:status IS NULL        OR b.status        = :status)
              AND (:paymentStatus IS NULL OR b.paymentStatus = :paymentStatus)
              AND (:bookingStatus IS NULL OR b.bookingStatus = :bookingStatus)
              AND (:from IS NULL          OR b.createdAt    >= :from)
              AND (:to   IS NULL          OR b.createdAt    <= :to)
            """)
    Page<Booking> adminSearch(
            @Param("search")        String search,
            @Param("status")        String status,
            @Param("paymentStatus") String paymentStatus,
            @Param("bookingStatus") String bookingStatus,
            @Param("from")          OffsetDateTime from,
            @Param("to")            OffsetDateTime to,
            Pageable pageable
    );

    // ----------------------------------------------------------------
    // Stats
    // ----------------------------------------------------------------
    long countByPaymentStatus(String paymentStatus);

    @Query("SELECT COUNT(b) FROM Booking b WHERE CAST(b.createdAt AS date) = :today")
    long countToday(@Param("today") LocalDate today);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Booking b WHERE b.paymentStatus = 'paid'")
    java.math.BigDecimal sumRevenue();
}
