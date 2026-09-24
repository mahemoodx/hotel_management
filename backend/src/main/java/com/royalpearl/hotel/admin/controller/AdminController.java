package com.royalpearl.hotel.admin.controller;

import com.royalpearl.hotel.admin.dto.AdminStatsDto;
import com.royalpearl.hotel.admin.dto.RoleRequest;
import com.royalpearl.hotel.admin.service.AdminService;
import com.royalpearl.hotel.booking.dto.BookingDto;
import com.royalpearl.hotel.booking.dto.UpdateBookingRequest;
import com.royalpearl.hotel.booking.service.BookingService;
import com.royalpearl.hotel.contact.dto.ContactMessageDto;
import com.royalpearl.hotel.contact.dto.UpdateContactMessageRequest;
import com.royalpearl.hotel.contact.entity.NewsletterSubscriber;
import com.royalpearl.hotel.contact.service.ContactService;
import com.royalpearl.hotel.order.dto.OrderDto;
import com.royalpearl.hotel.order.dto.UpdateOrderRequest;
import com.royalpearl.hotel.order.service.OrderService;
import com.royalpearl.hotel.user.dto.UserDto;
import com.royalpearl.hotel.user.entity.AppRole;
import com.royalpearl.hotel.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('admin')")
@Tag(name = "Admin", description = "Admin-only dashboard: stats, bookings, orders, messages, users")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private final AdminService   adminService;
    private final BookingService bookingService;
    private final OrderService   orderService;
    private final ContactService contactService;
    private final UserService    userService;

    // ================================================================
    // Stats
    // ================================================================

    @GetMapping("/stats")
    @Operation(summary = "Dashboard statistics snapshot")
    public ResponseEntity<AdminStatsDto> stats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    // ================================================================
    // Bookings
    // ================================================================

    @GetMapping("/bookings")
    @Operation(summary = "Search / list all bookings (paginated)")
    public ResponseEntity<Page<BookingDto>> listBookings(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) String bookingStatus,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(bookingService.adminSearch(
                search, status, paymentStatus, bookingStatus, from, to, pageable));
    }

    @GetMapping("/bookings/{id}")
    @Operation(summary = "Get a booking by internal UUID")
    public ResponseEntity<BookingDto> getBooking(@PathVariable UUID id) {
        return ResponseEntity.ok(bookingService.adminGetById(id));
    }

    @PatchMapping("/bookings/{id}")
    @Operation(summary = "Update booking status / payment status / notes")
    public ResponseEntity<BookingDto> updateBooking(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBookingRequest req) {
        return ResponseEntity.ok(bookingService.adminUpdate(id, req));
    }

    // ================================================================
    // Orders
    // ================================================================

    @GetMapping("/orders")
    @Operation(summary = "Search / list all food orders (paginated)")
    public ResponseEntity<Page<OrderDto>> listOrders(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) String orderStatus,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(orderService.adminSearch(
                search, status, paymentStatus, orderStatus, from, to, pageable));
    }

    @GetMapping("/orders/{id}")
    @Operation(summary = "Get a food order by internal UUID")
    public ResponseEntity<OrderDto> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.adminGetById(id));
    }

    @PatchMapping("/orders/{id}")
    @Operation(summary = "Update order status / payment status / notes")
    public ResponseEntity<OrderDto> updateOrder(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderRequest req) {
        return ResponseEntity.ok(orderService.adminUpdate(id, req));
    }

    // ================================================================
    // Contact messages
    // ================================================================

    @GetMapping("/messages")
    @Operation(summary = "List contact messages (paginated, filterable by status)")
    public ResponseEntity<Page<ContactMessageDto>> listMessages(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(contactService.adminSearch(status, search, pageable));
    }

    @PatchMapping("/messages/{id}")
    @Operation(summary = "Update a contact message status")
    public ResponseEntity<ContactMessageDto> updateMessage(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateContactMessageRequest req) {
        return ResponseEntity.ok(contactService.adminUpdate(id, req));
    }

    // ================================================================
    // Users
    // ================================================================

    @GetMapping("/users")
    @Operation(summary = "List all users (paginated, searchable)")
    public ResponseEntity<Page<UserDto>> listUsers(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(userService.adminListUsers(search, pageable));
    }

    @PostMapping("/users/{id}/roles")
    @Operation(summary = "Add a role to a user")
    public ResponseEntity<UserDto> addRole(
            @PathVariable UUID id,
            @Valid @RequestBody RoleRequest req) {
        return ResponseEntity.ok(userService.addRole(id, req.getRole()));
    }

    @DeleteMapping("/users/{id}/roles/{role}")
    @Operation(summary = "Remove a role from a user")
    public ResponseEntity<UserDto> removeRole(
            @PathVariable UUID id,
            @PathVariable AppRole role) {
        return ResponseEntity.ok(userService.removeRole(id, role));
    }

    // ================================================================
    // Newsletter
    // ================================================================

    @GetMapping("/newsletter")
    @Operation(summary = "List newsletter subscribers (paginated)")
    public ResponseEntity<Page<NewsletterSubscriber>> listNewsletter(
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(contactService.adminListNewsletter(pageable));
    }
}
