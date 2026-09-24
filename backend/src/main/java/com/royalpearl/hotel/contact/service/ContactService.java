package com.royalpearl.hotel.contact.service;

import com.royalpearl.hotel.contact.dto.*;
import com.royalpearl.hotel.contact.entity.ContactMessage;
import com.royalpearl.hotel.contact.entity.NewsletterSubscriber;
import com.royalpearl.hotel.contact.mapper.ContactMapper;
import com.royalpearl.hotel.contact.repository.ContactMessageRepository;
import com.royalpearl.hotel.contact.repository.NewsletterSubscriberRepository;
import com.royalpearl.hotel.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactMessageRepository       contactRepo;
    private final NewsletterSubscriberRepository newsletterRepo;
    private final ContactMapper                  contactMapper;

    // ---------------------------------------------------------------
    // Contact message
    // ---------------------------------------------------------------

    @Transactional
    public ContactMessageDto submitContact(ContactRequest req) {
        ContactMessage saved = contactRepo.save(ContactMessage.builder()
                .name(req.getName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .subject(req.getSubject())
                .message(req.getMessage())
                .status("new")
                .build());
        log.info("Contact message from {}", req.getEmail());
        return contactMapper.toDto(saved);
    }

    // ---------------------------------------------------------------
    // Newsletter
    // ---------------------------------------------------------------

    @Transactional
    public void subscribe(NewsletterRequest req) {
        if (!newsletterRepo.existsByEmail(req.getEmail().toLowerCase())) {
            newsletterRepo.save(NewsletterSubscriber.builder()
                    .email(req.getEmail().toLowerCase())
                    .build());
            log.info("Newsletter subscription: {}", req.getEmail());
        }
        // idempotent – already subscribed is a silent success
    }

    // ---------------------------------------------------------------
    // Admin
    // ---------------------------------------------------------------

    @Transactional(readOnly = true)
    public Page<ContactMessageDto> adminSearch(String status, String search, Pageable pageable) {
        String s = (status == null || status.isBlank()) ? null : status;
        String q = (search == null || search.isBlank()) ? null : search;
        return contactRepo.adminSearch(s, q, pageable)
                .map(contactMapper::toDto);
    }

    @Transactional
    public ContactMessageDto adminUpdate(UUID id, UpdateContactMessageRequest req) {
        ContactMessage msg = contactRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found: " + id));
        if (req.getStatus() != null) msg.setStatus(req.getStatus());
        return contactMapper.toDto(contactRepo.save(msg));
    }

    @Transactional(readOnly = true)
    public Page<NewsletterSubscriber> adminListNewsletter(Pageable pageable) {
        return newsletterRepo.findAllByOrderByCreatedAtDesc(pageable);
    }
}
