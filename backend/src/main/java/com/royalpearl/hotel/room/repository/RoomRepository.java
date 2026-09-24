package com.royalpearl.hotel.room.repository;

import com.royalpearl.hotel.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    /** Public room listing — only active rooms. */
    List<Room> findAllByActiveTrueOrderByPricePerNightAsc();

    Optional<Room> findBySlugAndActiveTrue(String slug);

    boolean existsBySlug(String slug);
}
