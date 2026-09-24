package com.royalpearl.hotel.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Phone is required")
    private String phone;

    @Pattern(regexp = "dine_in|takeaway|delivery", message = "orderType must be dine_in, takeaway or delivery")
    private String orderType;

    private String preferredTime;
    private String address;
    private String notes;

    @NotEmpty(message = "Cart must not be empty")
    @Valid
    private List<OrderItemDto> items;

    @NotNull(message = "Total is required")
    @Positive(message = "Total must be positive")
    private BigDecimal total;

    @Pattern(regexp = "cash|online", message = "Payment method must be 'cash' or 'online'")
    private String paymentMethod;

    // paymentStatus deliberately excluded — server always sets 'pending'
}
