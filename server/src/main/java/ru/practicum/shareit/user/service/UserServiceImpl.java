package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        log.info("Запрос на получение всех пользователей");
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        log.info("Запрос пользователя с id={}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id={} не найден", id);
                    return new NotFoundException("User not found");
                });
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        log.info("Создание пользователя: email='{}'", userDto.getEmail());
        validateEmail(userDto.getEmail());
        if (userRepository.existsByEmail(userDto.getEmail())) {
            log.warn("Email '{}' уже существует", userDto.getEmail());
            throw new EmailAlreadyExistsException(userDto.getEmail());
        }
        User user = UserMapper.toUser(userDto);
        User savedUser = userRepository.save(user);
        log.info("Пользователь создан с id={}", savedUser.getId());
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        log.info("Обновление пользователя с id={}", id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Пользователь с id={} не найден", id);
                    return new NotFoundException("User not found");
                });

        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }

        if (userDto.getEmail() != null) {
            String newEmail = userDto.getEmail().trim();
            if (newEmail.isBlank()) {
                throw new ValidationException("Email must not be blank");
            }
            validateEmail(newEmail);

            if (!newEmail.equals(existingUser.getEmail())) {
                if (userRepository.existsByEmail(newEmail)) {
                    log.warn("Попытка смены email на уже занятый: {}", newEmail);
                    throw new EmailAlreadyExistsException(newEmail);
                }
                existingUser.setEmail(newEmail);
            }
        }

        User updatedUser = userRepository.save(existingUser);
        log.info("Пользователь с id={} обновлён", id);
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Удаление пользователя с id={}", id);
        if (!userRepository.existsById(id)) {
            log.warn("Попытка удалить несуществующего пользователя с id={}", id);
            throw new NotFoundException("User not found");
        }
        userRepository.deleteById(id);
        log.info("Пользователь с id={} удалён", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userExists(Long userId) {
        return userRepository.existsById(userId);
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email must not be null or blank");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
            throw new ValidationException("Invalid email format: " + email);
        }
    }
}