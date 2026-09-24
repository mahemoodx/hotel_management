package com.royalpearl.hotel.menu.dto;

import com.royalpearl.hotel.menu.entity.MenuItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MenuItemMapper {

    @Mapping(target = "id", expression = "java(item.getId().toString())")
    MenuItemDto toDto(MenuItem item);

    List<MenuItemDto> toDtoList(List<MenuItem> items);
}
