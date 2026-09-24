package com.royalpearl.hotel.order.mapper;

import com.royalpearl.hotel.order.dto.OrderDto;
import com.royalpearl.hotel.order.dto.OrderItemDto;
import com.royalpearl.hotel.order.entity.OrderItem;
import com.royalpearl.hotel.order.entity.TableReservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "id", expression = "java(order.getId().toString())")
    @Mapping(target = "userId",
             expression = "java(order.getUser() != null ? order.getUser().getId().toString() : null)")
    OrderDto toDto(TableReservation order);

    List<OrderDto> toDtoList(List<TableReservation> orders);

    OrderItemDto toItemDto(OrderItem item);
    OrderItem toItem(OrderItemDto dto);

    List<OrderItemDto> toItemDtoList(List<OrderItem> items);
    List<OrderItem> toItemList(List<OrderItemDto> dtos);
}
