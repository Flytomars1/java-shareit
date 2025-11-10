package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.user.exception.ValidationException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.info("Запрос на получение всех пользователей");
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long id) {
        log.info("Запрос на получение пользователя с id={}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Попытка получить несуществующего пользователя с id={}", id);
                    return new ValidationException("User not found");
                });
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        log.info("Попытка создания пользователя: name='{}', email='{}'",
                userDto.getName(), userDto.getEmail());
        validateUser(userDto);
        checkEmailUnique(null, userDto.getEmail());
        User user = UserMapper.toUser(userDto);
        User savedUser = userRepository.save(user);
        log.info("Пользователь создан с id={}", savedUser.getId());
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        log.info("Попытка обновления пользователя с id={}", id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Попытка обновить несуществующего пользователя с id={}", id);
                    return new ValidationException("User not found");
                });

        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
            log.debug("Имя пользователя id={} обновлено на '{}'", id, userDto.getName());
        }

        if (userDto.getEmail() != null) {
            String newEmail = userDto.getEmail().trim();
            if (newEmail.isBlank()) {
                log.warn("Попытка установить пустой email для пользователя id={}", id);
                throw new ValidationException("Email must not be blank");
            }
            validateEmail(newEmail);

            if (!newEmail.equals(existingUser.getEmail())) {
                checkEmailUnique(id, newEmail);
                existingUser.setEmail(newEmail);
                log.debug("Email пользователя id={} обновлён на '{}'", id, newEmail);
            }
        }

        User updatedUser = userRepository.save(existingUser);
        log.info("Пользователь с id={} успешно обновлён", id);
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Попытка удаления пользователя с id={}", id);
        if (!userRepository.existsById(id)) {
            log.warn("Попытка удалить несуществующего пользователя с id={}", id);
            throw new ValidationException("User not found");
        }
        userRepository.deleteById(id);
        log.info("Пользователь с id={} успешно удалён", id);
    }

    @Override
    public boolean userExists(Long userId) {
        return userRepository.existsById(userId);
    }

    private void validateUser(UserDto userDto) {
        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new ValidationException("Email must not be blank");
        }
        validateEmail(userDto.getEmail());
    }

    private void validateEmail(String email) {
        if (!email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
            throw new ValidationException("Invalid email format: " + email);
        }
    }

    private void checkEmailUnique(Long userId, String email) {
        if (userRepository.existsByEmail(email)) {
            if (userId == null) {
                throw new EmailAlreadyExistsException(email);
            }
            for (User user : userRepository.findAll()) {
                if (email.equals(user.getEmail()) && !userId.equals(user.getId())) {
                    throw new EmailAlreadyExistsException(email);
                }
            }
        }
    }
}