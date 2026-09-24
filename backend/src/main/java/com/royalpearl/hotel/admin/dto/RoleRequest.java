package com.royalpearl.hotel.admin.dto;

import com.royalpearl.hotel.user.entity.AppRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoleRequest {

    @NotNull(message = "Role is required")
    private AppRole role;
}
