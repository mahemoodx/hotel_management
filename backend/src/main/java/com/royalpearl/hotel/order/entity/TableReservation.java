package com.royalpearl.hotel.order.entity;

import com.royalpearl.hotel.user.entity.User;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "table_reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Human-readable reference: RPO- + 7 digits, e.g. RPO-7710284.
     * Generated server-side in OrderService.
     */
    @Column(name = "order_id", unique = true)
    private String orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(name = "order_type")
    private String orderType;

    @Column(name = "preferred_time")
    private String preferredTime;

    private String address;

    private String notes;

    /**
     * Cart items stored as JSONB: [{name, price, qty}, ...].
     */
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<OrderItem> items;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(nullable = false)
    @Builder.Default
    private String status = "new";

    @Column(name = "order_status", nullable = false)
    @Builder.Default
    private String orderStatus = "pending";

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
