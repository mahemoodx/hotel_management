package com.royalpearl.hotel.order.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
public class OrderDto {
    private String id;
    private String orderId;
    private String userId;
    private String name;
    private String phone;
    private String orderType;
    private String preferredTime;
    private String address;
    private String notes;
    private List<OrderItemDto> items;
    private BigDecimal total;
    private String status;
    private String orderStatus;
    private String paymentMethod;
    private String paymentStatus;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
