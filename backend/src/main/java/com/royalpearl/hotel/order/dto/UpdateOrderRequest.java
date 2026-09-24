package com.royalpearl.hotel.order.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Admin-only PATCH payload. All fields are optional (null = no change).
 */
@Data
public class UpdateOrderRequest {

    @Pattern(regexp = "new|reviewed", message = "status must be 'new' or 'reviewed'")
    private String status;

    @Pattern(regexp = "pending|preparing|ready|delivered|cancelled",
             message = "Invalid order status")
    private String orderStatus;

    @Pattern(regexp = "pending|paid|refunded", message = "Invalid payment status")
    private String paymentStatus;

    private String notes;
}
