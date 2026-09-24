package com.royalpearl.hotel.menu.controller;

import com.royalpearl.hotel.menu.dto.MenuItemDto;
import com.royalpearl.hotel.menu.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/menu")
@RequiredArgsConstructor
@Tag(name = "Menu", description = "Public restaurant menu")
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    @Operation(summary = "List available menu items; filter by ?category= and/or ?veg=true|false")
    public ResponseEntity<List<MenuItemDto>> getMenu(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean veg) {
        return ResponseEntity.ok(menuService.getMenu(category, veg));
    }
}
