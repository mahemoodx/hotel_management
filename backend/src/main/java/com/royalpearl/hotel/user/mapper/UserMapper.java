package com.royalpearl.hotel.user.mapper;

import com.royalpearl.hotel.user.dto.UserDto;
import com.royalpearl.hotel.user.entity.AppRole;
import com.royalpearl.hotel.user.entity.User;
import com.royalpearl.hotel.user.entity.UserRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", expression = "java(user.getId().toString())")
    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToStrings")
    UserDto toDto(User user);

    List<UserDto> toDtoList(List<User> users);

    @Named("rolesToStrings")
    static List<String> rolesToStrings(Set<UserRole> roles) {
        if (roles == null) return List.of();
        return roles.stream()
                    .map(r -> r.getRole().name())
                    .collect(Collectors.toList());
    }
}
