package ru.practicum.service;

import ru.practicum.model.dto.user.UserDto;
import ru.practicum.model.entity.User;

import java.util.List;

public interface UserService {
    List<UserDto> getAll(List<Long> ids, Integer from, Integer size);

    UserDto create(UserDto user);

    void delete(Long userId);

    UserDto findById(Long userId);

    User findUserById(Long userId);
}
