package com.royalpearl.hotel.booking.controller;

import com.royalpearl.hotel.booking.dto.BookingDto;
import com.royalpearl.hotel.booking.dto.CreateBookingRequest;
import com.royalpearl.hotel.booking.service.BookingService;
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
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Create room bookings (guest and authenticated) and lookup by reference")
public class BookingController {

    private final BookingService bookingService;

    /**
     * Public: both guests and authenticated users can POST.
     * The service attaches the user FK when a valid token is present.
     */
    @PostMapping
    @Operation(summary = "Create a room booking (guest or authenticated)")
    public ResponseEntity<BookingDto> createBooking(@Valid @RequestBody CreateBookingRequest req) {
        UUID userId = SecurityUtil.currentUserId().orElse(null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(req, userId));
    }

    /** Public: status lookup by reference + phone — no token required. */
    @GetMapping("/lookup")
    @Operation(summary = "Look up booking status by reference code and phone number")
    public ResponseEntity<BookingDto> lookup(
            @RequestParam String reference,
            @RequestParam String phone) {
        return ResponseEntity.ok(bookingService.lookup(reference, phone));
    }

    /** Authenticated: own bookings (paginated). */
    @GetMapping("/me")
    @Operation(summary = "List the current user's bookings",
               security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<BookingDto>> myBookings(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        UUID userId = SecurityUtil.requireCurrentUserId();
        return ResponseEntity.ok(bookingService.getMyBookings(userId, pageable));
    }
}
