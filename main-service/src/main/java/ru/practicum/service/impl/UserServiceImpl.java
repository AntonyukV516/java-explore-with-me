package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.dto.user.UserDto;
import ru.practicum.model.entity.User;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.UserService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserDto> getAll(List<Long> ids, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);

        Page<User> usersPage = (ids != null && !ids.isEmpty())
                ? userRepository.findByIdIn(ids, pageable)
                : userRepository.findAll(pageable);

        return usersPage.getContent()
                .stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public UserDto create(UserDto user) {
        Optional<User> userByEmail = userRepository.findByEmail(user.getEmail());
        if (userByEmail.isPresent()) {
            throw new ConditionsNotMetException("Пользователь с таким email уже существует");
        }
        return UserMapper.toUserDto(
                userRepository.save(UserMapper.toUser(user))
        );
    }

    @Override
    public void delete(Long userId) {
        userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id=" + userId + " не найден")
        );
        userRepository.deleteById(userId);
    }

    @Override
    public UserDto findById(Long userId) {
        return UserMapper.toUserDto(findUserById(userId));
    }

    @Override
    public User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователя с id " + userId + " не существует."));
    }
}