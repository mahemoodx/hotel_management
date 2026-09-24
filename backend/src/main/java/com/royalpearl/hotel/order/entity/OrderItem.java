package com.royalpearl.hotel.order.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents one cart line inside a table_reservation's items JSONB array.
 * Serialised/deserialised via Jackson – not a JPA entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private String name;
    private BigDecimal price;
    private int qty;
}
