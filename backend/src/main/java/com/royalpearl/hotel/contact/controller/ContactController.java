package com.royalpearl.hotel.contact.controller;

import com.royalpearl.hotel.common.ApiResponse;
import com.royalpearl.hotel.contact.dto.ContactMessageDto;
import com.royalpearl.hotel.contact.dto.ContactRequest;
import com.royalpearl.hotel.contact.dto.NewsletterRequest;
import com.royalpearl.hotel.contact.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Contact & Newsletter", description = "Contact form and newsletter subscription")
public class ContactController {

    private final ContactService contactService;

    @PostMapping("/contact")
    @Operation(summary = "Submit a contact / enquiry message")
    public ResponseEntity<ContactMessageDto> contact(@Valid @RequestBody ContactRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contactService.submitContact(req));
    }

    @PostMapping("/newsletter")
    @Operation(summary = "Subscribe to the newsletter (idempotent)")
    public ResponseEntity<ApiResponse<Void>> newsletter(@Valid @RequestBody NewsletterRequest req) {
        contactService.subscribe(req);
        return ResponseEntity.ok(ApiResponse.ok("Subscribed successfully"));
    }
}
