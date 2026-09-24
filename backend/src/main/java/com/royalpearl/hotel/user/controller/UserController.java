package com.royalpearl.hotel.user.controller;

import com.royalpearl.hotel.booking.dto.BookingDto;
import com.royalpearl.hotel.booking.service.BookingService;
import com.royalpearl.hotel.order.dto.OrderDto;
import com.royalpearl.hotel.order.service.OrderService;
import com.royalpearl.hotel.security.SecurityUtil;
import com.royalpearl.hotel.user.dto.UpdateProfileRequest;
import com.royalpearl.hotel.user.dto.UserDto;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
@Tag(name = "My Account", description = "Authenticated user profile, bookings and orders")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService    userService;
    private final BookingService bookingService;
    private final OrderService   orderService;

    // ---- Profile -------------------------------------------------------

    @GetMapping
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserDto> getMe() {
        UUID userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(userService.getMe(userId));
    }

    @PatchMapping
    @Operation(summary = "Update current user profile")
    public ResponseEntity<UserDto> updateMe(@Valid @RequestBody UpdateProfileRequest req) {
        UUID userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(userService.updateMe(userId, req));
    }

    // ---- Own bookings --------------------------------------------------

    @GetMapping("/bookings")
    @Operation(summary = "List current user's room bookings (paginated)")
    public ResponseEntity<Page<BookingDto>> myBookings(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        UUID userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(bookingService.getMyBookings(userId, pageable));
    }

    // ---- Own orders ----------------------------------------------------

    @GetMapping("/orders")
    @Operation(summary = "List current user's food orders (paginated)")
    public ResponseEntity<Page<OrderDto>> myOrders(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        UUID userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(orderService.getMyOrders(userId, pageable));
    }

    // ---- Claim admin ---------------------------------------------------

    @PostMapping("/claim-admin")
    @Operation(summary = "Grant admin role if email is on the allowlist")
    public ResponseEntity<UserDto> claimAdmin() {
        UUID userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(userService.claimAdmin(userId));
    }
}
