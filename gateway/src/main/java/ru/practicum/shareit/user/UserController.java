package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> createUser(@RequestBody UserDto userDto) {
        log.info("Получен POST-запрос на /users: name='{}', email='{}'", userDto.getName(), userDto.getEmail());
        return userClient.create(userDto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateUser(
            @PathVariable Long id,
            @RequestHeader(value = USER_ID_HEADER, required = false) Long userId,
            @RequestBody UserDto userDto) {
        log.info("Получен PATCH-запрос на /users/{} от пользователя с id={}", id, userId);
        return userClient.update(id, userId, userDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUser(
            @PathVariable Long id,
            @RequestHeader(value = USER_ID_HEADER, required = false) Long userId) {
        log.info("Получен GET-запрос на /users/{} от пользователя с id={}", id, userId);
        return userClient.getUser(id);
    }

    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
        log.info("Получен GET-запрос на /users");
        return userClient.getAllUsers();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteUser(
            @PathVariable Long id,
            @RequestHeader(value = USER_ID_HEADER, required = false) Long userId) {
        log.info("Получен DELETE-запрос на /users/{} от пользователя с id={}", id, userId);
        return userClient.delete(id, userId);
    }
}