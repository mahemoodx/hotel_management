package com.royalpearl.hotel.booking.mapper;

import com.royalpearl.hotel.booking.dto.BookingDto;
import com.royalpearl.hotel.booking.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "id", expression = "java(booking.getId().toString())")
    @Mapping(target = "userId",
             expression = "java(booking.getUser() != null ? booking.getUser().getId().toString() : null)")
    BookingDto toDto(Booking booking);

    List<BookingDto> toDtoList(List<Booking> bookings);
}
