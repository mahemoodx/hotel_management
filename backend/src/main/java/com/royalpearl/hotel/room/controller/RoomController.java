package com.royalpearl.hotel.room.controller;

import com.royalpearl.hotel.room.dto.RoomDto;
import com.royalpearl.hotel.room.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
@Tag(name = "Rooms", description = "Public room listing")
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    @Operation(summary = "List all active rooms ordered by price")
    public ResponseEntity<List<RoomDto>> listRooms() {
        return ResponseEntity.ok(roomService.listActiveRooms());
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get a single active room by slug")
    public ResponseEntity<RoomDto> getRoom(@PathVariable String slug) {
        return ResponseEntity.ok(roomService.getRoomBySlug(slug));
    }
}
