package com.royalpearl.hotel.booking.service;

import com.royalpearl.hotel.booking.dto.BookingDto;
import com.royalpearl.hotel.booking.dto.CreateBookingRequest;
import com.royalpearl.hotel.booking.dto.UpdateBookingRequest;
import com.royalpearl.hotel.booking.entity.Booking;
import com.royalpearl.hotel.booking.mapper.BookingMapper;
import com.royalpearl.hotel.booking.repository.BookingRepository;
import com.royalpearl.hotel.common.ReferenceGenerator;
import com.royalpearl.hotel.exception.ResourceNotFoundException;
import com.royalpearl.hotel.user.entity.User;
import com.royalpearl.hotel.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private static final int MAX_REF_ATTEMPTS = 10;

    private final BookingRepository bookingRepository;
    private final UserRepository    userRepository;
    private final BookingMapper     bookingMapper;

    // ---------------------------------------------------------------
    // Public – guest/authenticated create
    // ---------------------------------------------------------------

    @Transactional
    public BookingDto createBooking(CreateBookingRequest req, UUID authenticatedUserId) {

        Booking booking = Booking.builder()
                .bookingId(generateUniqueRef())
                .fullName(req.getFullName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .roomName(req.getRoomName())
                .checkIn(req.getCheckIn())
                .checkOut(req.getCheckOut())
                .guests(req.getGuests())
                .arrival(req.getArrival())
                .notes(req.getNotes())
                .roomPrice(req.getRoomPrice())
                .totalAmount(req.getTotalAmount())
                .paymentMethod(Optional.ofNullable(req.getPaymentMethod()).orElse("cash"))
                // paymentStatus is ALWAYS 'pending' on creation – client cannot override
                .paymentStatus("pending")
                .status("new")
                .bookingStatus("pending")
                .build();

        // Attach user if authenticated
        if (authenticatedUserId != null) {
            userRepository.findById(authenticatedUserId)
                    .ifPresent(booking::setUser);
        }

        Booking saved = bookingRepository.save(booking);
        log.info("Booking created: {} by user={}", saved.getBookingId(), authenticatedUserId);
        return bookingMapper.toDto(saved);
    }

    // ---------------------------------------------------------------
    // Guest lookup (no auth)
    // ---------------------------------------------------------------

    @Transactional(readOnly = true)
    public BookingDto lookup(String reference, String phone) {
        return bookingRepository.findByBookingIdAndPhone(reference, phone)
                .map(bookingMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No booking found for reference " + reference));
    }

    // ---------------------------------------------------------------
    // Authenticated user – own bookings
    // ---------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<BookingDto> getMyBookings(UUID userId, Pageable pageable) {
        return bookingRepository
                .findAllByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(bookingMapper::toDto);
    }

    // ---------------------------------------------------------------
    // Admin
    // ---------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<BookingDto> adminSearch(String search, String status, String paymentStatus,
                                        String bookingStatus,
                                        OffsetDateTime from, OffsetDateTime to,
                                        Pageable pageable) {
        return bookingRepository
                .adminSearch(
                        blankToNull(search), blankToNull(status),
                        blankToNull(paymentStatus), blankToNull(bookingStatus),
                        from, to, pageable)
                .map(bookingMapper::toDto);
    }

    @Transactional(readOnly = true)
    public BookingDto adminGetById(UUID id) {
        return bookingRepository.findById(id)
                .map(bookingMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));
    }

    @Transactional
    public BookingDto adminUpdate(UUID id, UpdateBookingRequest req) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + id));

        if (req.getStatus() != null)        booking.setStatus(req.getStatus());
        if (req.getBookingStatus() != null) booking.setBookingStatus(req.getBookingStatus());
        if (req.getPaymentStatus() != null) booking.setPaymentStatus(req.getPaymentStatus());
        if (req.getNotes() != null)         booking.setNotes(req.getNotes());

        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private String generateUniqueRef() {
        for (int i = 0; i < MAX_REF_ATTEMPTS; i++) {
            String ref = ReferenceGenerator.bookingRef();
            if (!bookingRepository.existsByBookingId(ref)) return ref;
        }
        throw new IllegalStateException("Could not generate unique booking reference");
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
