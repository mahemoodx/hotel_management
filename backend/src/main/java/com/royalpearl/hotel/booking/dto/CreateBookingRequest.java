package com.royalpearl.hotel.booking.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateBookingRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    @NotBlank(message = "Phone is required")
    private String phone;

    private String roomName;

    private LocalDate checkIn;
    private LocalDate checkOut;

    @Min(value = 1, message = "Guests must be at least 1")
    private Integer guests;

    private LocalTime arrival;
    private String notes;
    private BigDecimal roomPrice;
    private BigDecimal totalAmount;

    @Pattern(regexp = "cash|online", message = "Payment method must be 'cash' or 'online'")
    private String paymentMethod;

    // paymentStatus deliberately excluded — server always sets 'pending'
}
