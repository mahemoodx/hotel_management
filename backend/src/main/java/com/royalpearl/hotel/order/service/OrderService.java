package com.royalpearl.hotel.order.service;

import com.royalpearl.hotel.common.ReferenceGenerator;
import com.royalpearl.hotel.exception.ResourceNotFoundException;
import com.royalpearl.hotel.order.dto.CreateOrderRequest;
import com.royalpearl.hotel.order.dto.OrderDto;
import com.royalpearl.hotel.order.dto.UpdateOrderRequest;
import com.royalpearl.hotel.order.entity.OrderItem;
import com.royalpearl.hotel.order.entity.TableReservation;
import com.royalpearl.hotel.order.mapper.OrderMapper;
import com.royalpearl.hotel.order.repository.TableReservationRepository;
import com.royalpearl.hotel.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final int MAX_REF_ATTEMPTS = 10;

    private final TableReservationRepository orderRepository;
    private final UserRepository             userRepository;
    private final OrderMapper                orderMapper;

    // ---------------------------------------------------------------
    // Public – guest/authenticated create
    // ---------------------------------------------------------------

    @Transactional
    public OrderDto createOrder(CreateOrderRequest req, UUID authenticatedUserId) {

        List<OrderItem> items = orderMapper.toItemList(req.getItems());

        TableReservation order = TableReservation.builder()
                .orderId(generateUniqueRef())
                .name(req.getName())
                .phone(req.getPhone())
                .orderType(req.getOrderType())
                .preferredTime(req.getPreferredTime())
                .address(req.getAddress())
                .notes(req.getNotes())
                .items(items)
                .total(req.getTotal())
                .paymentMethod(Optional.ofNullable(req.getPaymentMethod()).orElse("cash"))
                // paymentStatus is ALWAYS 'pending' on creation – client cannot override
                .paymentStatus("pending")
                .status("new")
                .orderStatus("pending")
                .build();

        if (authenticatedUserId != null) {
            userRepository.findById(authenticatedUserId)
                    .ifPresent(order::setUser);
        }

        TableReservation saved = orderRepository.save(order);
        log.info("Order created: {} by user={}", saved.getOrderId(), authenticatedUserId);
        return orderMapper.toDto(saved);
    }

    // ---------------------------------------------------------------
    // Guest lookup
    // ---------------------------------------------------------------

    @Transactional(readOnly = true)
    public OrderDto lookup(String reference, String phone) {
        return orderRepository.findByOrderIdAndPhone(reference, phone)
                .map(orderMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No order found for reference " + reference));
    }

    // ---------------------------------------------------------------
    // Authenticated user – own orders
    // ---------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<OrderDto> getMyOrders(UUID userId, Pageable pageable) {
        return orderRepository
                .findAllByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(orderMapper::toDto);
    }

    // ---------------------------------------------------------------
    // Admin
    // ---------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<OrderDto> adminSearch(String search, String status, String paymentStatus,
                                      String orderStatus,
                                      OffsetDateTime from, OffsetDateTime to,
                                      Pageable pageable) {
        return orderRepository
                .adminSearch(
                        blankToNull(search), blankToNull(status),
                        blankToNull(paymentStatus), blankToNull(orderStatus),
                        from, to, pageable)
                .map(orderMapper::toDto);
    }

    @Transactional(readOnly = true)
    public OrderDto adminGetById(UUID id) {
        return orderRepository.findById(id)
                .map(orderMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    @Transactional
    public OrderDto adminUpdate(UUID id, UpdateOrderRequest req) {
        TableReservation order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));

        if (req.getStatus() != null)      order.setStatus(req.getStatus());
        if (req.getOrderStatus() != null) order.setOrderStatus(req.getOrderStatus());
        if (req.getPaymentStatus() != null) order.setPaymentStatus(req.getPaymentStatus());
        if (req.getNotes() != null)       order.setNotes(req.getNotes());

        return orderMapper.toDto(orderRepository.save(order));
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private String generateUniqueRef() {
        for (int i = 0; i < MAX_REF_ATTEMPTS; i++) {
            String ref = ReferenceGenerator.orderRef();
            if (!orderRepository.existsByOrderId(ref)) return ref;
        }
        throw new IllegalStateException("Could not generate unique order reference");
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
