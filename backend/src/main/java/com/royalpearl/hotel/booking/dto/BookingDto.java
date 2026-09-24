package com.royalpearl.hotel.booking.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Getter
@Builder
public class BookingDto {
    private String id;
    private String bookingId;
    private String userId;
    private String fullName;
    private String email;
    private String phone;
    private String roomName;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Integer guests;
    private LocalTime arrival;
    private String notes;
    private BigDecimal roomPrice;
    private BigDecimal totalAmount;
    private String status;
    private String bookingStatus;
    private String paymentMethod;
    private String paymentStatus;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
