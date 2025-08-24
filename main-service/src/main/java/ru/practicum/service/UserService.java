package ru.practicum.service;

import ru.practicum.model.dto.RequestUserDto;
import ru.practicum.model.dto.ResponseUserDto;

import java.util.List;

public interface UserService {

    List<ResponseUserDto> getUsers(List<Long> ids, Integer from, Integer size);

    ResponseUserDto registerUser(RequestUserDto requestUserDto);

    void deleteUser(Long userId);
}
