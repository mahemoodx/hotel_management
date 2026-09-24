package com.royalpearl.hotel.room.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
public class RoomDto {
    private String id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal pricePerNight;
    private int maxGuests;
    private String imageUrl;
    private List<String> amenities;
    private boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
