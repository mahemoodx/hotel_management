package com.royalpearl.hotel.contact.repository;

import com.royalpearl.hotel.contact.entity.NewsletterSubscriber;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, UUID> {

    boolean existsByEmail(String email);

    Optional<NewsletterSubscriber> findByEmail(String email);

    Page<NewsletterSubscriber> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
