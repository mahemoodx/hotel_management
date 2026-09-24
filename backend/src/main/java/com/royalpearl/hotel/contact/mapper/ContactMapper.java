package com.royalpearl.hotel.contact.mapper;

import com.royalpearl.hotel.contact.dto.ContactMessageDto;
import com.royalpearl.hotel.contact.entity.ContactMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContactMapper {

    @Mapping(target = "id", expression = "java(msg.getId().toString())")
    ContactMessageDto toDto(ContactMessage msg);

    List<ContactMessageDto> toDtoList(List<ContactMessage> messages);
}
