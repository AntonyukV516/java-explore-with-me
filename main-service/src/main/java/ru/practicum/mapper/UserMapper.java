package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.practicum.model.dto.RequestUserDto;
import ru.practicum.model.dto.ResponseUserDto;
import ru.practicum.model.dto.ResponseUserShortDto;
import ru.practicum.model.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(RequestUserDto requestUserDto);

    ResponseUserDto toDto(User user);

    ResponseUserShortDto toShortDto(User user);

    @Mapping(target = "id", ignore = true)
    void updateUserFromRequest(RequestUserDto request, @MappingTarget User user);
}
