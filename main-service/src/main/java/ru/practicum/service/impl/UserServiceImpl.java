package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.dto.RequestUserDto;
import ru.practicum.model.dto.ResponseUserDto;
import ru.practicum.model.entity.User;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ResponseUserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);

        if (ids != null && !ids.isEmpty()) {
            return userRepository.findAllByIdIn(ids, pageRequest).stream()
                    .map(userMapper::toDto)
                    .collect(Collectors.toList());
        }

        return userRepository.findAll(pageRequest).stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ResponseUserDto registerUser(RequestUserDto requestUserDto) {
        checkEmailUnique(requestUserDto.getEmail());
        User user = userMapper.toEntity(requestUserDto);
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    public void deleteUser(Long userId) {
        checkUserExists(userId);
        userRepository.deleteById(userId);
    }

    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
    }

    private void checkEmailUnique(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("User with email='" + email + "' already exists");
        }
    }
}
