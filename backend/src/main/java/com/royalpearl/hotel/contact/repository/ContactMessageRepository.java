package com.royalpearl.hotel.contact.repository;

import com.royalpearl.hotel.contact.entity.ContactMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, UUID> {

    long countByStatus(String status);

    @Query("""
            SELECT m FROM ContactMessage m
            WHERE (:status IS NULL OR m.status = :status)
              AND (:search IS NULL
                   OR LOWER(m.name)    LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(m.email)   LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(m.subject) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY m.createdAt DESC
            """)
    Page<ContactMessage> adminSearch(
            @Param("status") String status,
            @Param("search") String search,
            Pageable pageable
    );
}
