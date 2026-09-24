package com.royalpearl.hotel.room.service;

import com.royalpearl.hotel.exception.ResourceNotFoundException;
import com.royalpearl.hotel.room.dto.RoomDto;
import com.royalpearl.hotel.room.mapper.RoomMapper;
import com.royalpearl.hotel.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper     roomMapper;

    public List<RoomDto> listActiveRooms() {
        return roomMapper.toDtoList(
                roomRepository.findAllByActiveTrueOrderByPricePerNightAsc());
    }

    public RoomDto getRoomBySlug(String slug) {
        return roomRepository.findBySlugAndActiveTrue(slug)
                .map(roomMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found: " + slug));
    }
}
