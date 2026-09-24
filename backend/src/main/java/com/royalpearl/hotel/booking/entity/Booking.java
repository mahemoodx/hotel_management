package com.royalpearl.hotel.booking.entity;

import com.royalpearl.hotel.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Human-readable reference: RP + 7 digits, e.g. RP4820193.
     * Generated server-side in BookingService.
     */
    @Column(name = "booking_id", unique = true)
    private String bookingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(name = "room_name")
    private String roomName;

    @Column(name = "check_in")
    private LocalDate checkIn;

    @Column(name = "check_out")
    private LocalDate checkOut;

    private Integer guests;

    private LocalTime arrival;

    private String notes;

    @Column(name = "room_price", precision = 10, scale = 2)
    private BigDecimal roomPrice;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    @Builder.Default
    private String status = "new";

    @Column(name = "booking_status", nullable = false)
    @Builder.Default
    private String bookingStatus = "pending";

    @Column(name = "payment_method", nullable = false)
    @Builder.Default
    private String paymentMethod = "cash";

    /**
     * NEVER set by clients. Always starts as 'pending'.
     * Only ADMIN may upgrade to 'paid' or 'refunded'.
     */
    @Column(name = "payment_status", nullable = false)
    @Builder.Default
    private String paymentStatus = "pending";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
