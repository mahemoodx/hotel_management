package com.royalpearl.hotel.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
public class UserDto {
    private String id;
    private String email;
    private String fullName;
    private String phone;
    private String avatarUrl;
    private String provider;
    private boolean emailVerified;
    private List<String> roles;
    private OffsetDateTime createdAt;
}
