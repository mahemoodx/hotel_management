package com.royalpearl.hotel.contact.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateContactMessageRequest {

    @Pattern(regexp = "new|read|replied|archived", message = "Invalid status")
    private String status;
}
