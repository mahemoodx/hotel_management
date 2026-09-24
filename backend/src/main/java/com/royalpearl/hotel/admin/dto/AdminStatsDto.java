package com.royalpearl.hotel.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AdminStatsDto {
    private long totalBookings;
    private long todayBookings;
    private long pendingPayments;
    private long totalOrders;
    private BigDecimal revenue;          // sum of paid bookings + orders
    private long newMessages;
    private long totalUsers;
}
