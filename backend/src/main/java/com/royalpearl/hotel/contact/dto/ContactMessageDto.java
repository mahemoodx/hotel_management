package com.royalpearl.hotel.contact.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class ContactMessageDto {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String subject;
    private String message;
    private String status;
    private OffsetDateTime createdAt;
}
