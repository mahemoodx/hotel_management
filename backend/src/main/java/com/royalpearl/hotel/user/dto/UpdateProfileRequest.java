package com.royalpearl.hotel.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(max = 200, message = "Full name too long")
    private String fullName;

    @Size(max = 20, message = "Phone number too long")
    private String phone;

    private String avatarUrl;
}
