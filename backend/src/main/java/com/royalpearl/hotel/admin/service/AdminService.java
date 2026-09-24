package com.royalpearl.hotel.admin.service;

import com.royalpearl.hotel.admin.dto.AdminStatsDto;
import com.royalpearl.hotel.booking.repository.BookingRepository;
import com.royalpearl.hotel.contact.repository.ContactMessageRepository;
import com.royalpearl.hotel.order.repository.TableReservationRepository;
import com.royalpearl.hotel.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final BookingRepository         bookingRepo;
    private final TableReservationRepository orderRepo;
    private final ContactMessageRepository  contactRepo;
    private final UserRepository            userRepo;

    public AdminStatsDto getStats() {
        long totalBookings    = bookingRepo.count();
        long todayBookings    = bookingRepo.countToday(LocalDate.now());
        long pendingPayments  = bookingRepo.countByPaymentStatus("pending")
                              + orderRepo.countByPaymentStatus("pending");
        long totalOrders      = orderRepo.count();
        long newMessages      = contactRepo.countByStatus("new");
        long totalUsers       = userRepo.count();

        BigDecimal bookingRev = bookingRepo.sumRevenue();
        BigDecimal orderRev   = orderRepo.sumRevenue();
        BigDecimal revenue    = (bookingRev == null ? BigDecimal.ZERO : bookingRev)
                              .add(orderRev == null ? BigDecimal.ZERO : orderRev);

        return AdminStatsDto.builder()
                .totalBookings(totalBookings)
                .todayBookings(todayBookings)
                .pendingPayments(pendingPayments)
                .totalOrders(totalOrders)
                .revenue(revenue)
                .newMessages(newMessages)
                .totalUsers(totalUsers)
                .build();
    }
}
