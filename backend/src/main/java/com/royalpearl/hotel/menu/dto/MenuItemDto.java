package com.royalpearl.hotel.menu.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
public class MenuItemDto {
    private String id;
    private String name;
    private String category;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private boolean veg;
    private boolean available;
    private Integer sortOrder;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
