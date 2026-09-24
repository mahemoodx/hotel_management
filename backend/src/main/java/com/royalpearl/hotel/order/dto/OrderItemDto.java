package com.royalpearl.hotel.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDto {

    @NotBlank(message = "Item name is required")
    private String name;

    @NotNull(message = "Item price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int qty;
}
