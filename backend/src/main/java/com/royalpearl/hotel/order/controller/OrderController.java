package com.royalpearl.hotel.order.controller;

import com.royalpearl.hotel.order.dto.CreateOrderRequest;
import com.royalpearl.hotel.order.dto.OrderDto;
import com.royalpearl.hotel.order.service.OrderService;
import com.royalpearl.hotel.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Food Orders", description = "Place food orders (guest and authenticated) and lookup by reference")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Place a food order (guest or authenticated)")
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderRequest req) {
        UUID userId = SecurityUtil.currentUserId().orElse(null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(req, userId));
    }

    @GetMapping("/lookup")
    @Operation(summary = "Look up order status by reference code and phone number")
    public ResponseEntity<OrderDto> lookup(
            @RequestParam String reference,
            @RequestParam String phone) {
        return ResponseEntity.ok(orderService.lookup(reference, phone));
    }

    @GetMapping("/me")
    @Operation(summary = "List the current user's food orders",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<OrderDto>> myOrders(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        UUID userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(orderService.getMyOrders(userId, pageable));
    }
}
