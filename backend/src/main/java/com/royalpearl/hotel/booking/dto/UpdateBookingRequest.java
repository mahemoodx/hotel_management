package com.royalpearl.hotel.booking.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Admin-only PATCH payload. All fields are optional (null = no change).
 * paymentStatus may only be set by an admin (enforced in service layer).
 */
@Data
public class UpdateBookingRequest {

    @Pattern(regexp = "new|reviewed", message = "status must be 'new' or 'reviewed'")
    private String status;

    @Pattern(regexp = "pending|confirmed|checked_in|checked_out|cancelled",
             message = "Invalid booking status")
    private String bookingStatus;

    @Pattern(regexp = "pending|paid|refunded", message = "Invalid payment status")
    private String paymentStatus;

    private String notes;
}
