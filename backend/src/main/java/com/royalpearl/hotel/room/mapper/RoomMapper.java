package com.royalpearl.hotel.room.mapper;

import com.royalpearl.hotel.room.dto.RoomDto;
import com.royalpearl.hotel.room.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    @Mapping(target = "id", expression = "java(room.getId().toString())")
    RoomDto toDto(Room room);

    List<RoomDto> toDtoList(List<Room> rooms);
}
