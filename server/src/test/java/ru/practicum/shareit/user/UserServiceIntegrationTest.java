package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserServiceIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void createUser_ShouldSaveUser_WhenValidInput() {
        UserDto userDto = new UserDto();
        userDto.setName("Test User");
        userDto.setEmail("test@example.com");

        UserDto result = userService.createUser(userDto);

        assertNotNull(result.getId());
        assertEquals("Test User", result.getName());
        assertEquals("test@example.com", result.getEmail());

        // Проверяем, что пользователь действительно сохранился в базе
        List<User> usersInDb = userRepository.findAll();
        assertEquals(1, usersInDb.size());
        assertEquals("test@example.com", usersInDb.get(0).getEmail());
    }

    @Test
    void createUser_ShouldThrowValidationException_WhenEmailIsInvalid() {
        UserDto userDto = new UserDto();
        userDto.setName("Test User");
        userDto.setEmail("invalid-email");

        assertThrows(RuntimeException.class, () -> userService.createUser(userDto));
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user = userRepository.save(user);

        UserDto result = userService.getUserById(user.getId());

        assertEquals(user.getId(), result.getId());
        assertEquals("Test User", result.getName());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void getUserById_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        assertThrows(NotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        User user1 = new User();
        user1.setName("User 1");
        user1.setEmail("user1@example.com");
        userRepository.save(user1);

        User user2 = new User();
        user2.setName("User 2");
        user2.setEmail("user2@example.com");
        userRepository.save(user2);

        List<UserDto> result = userService.getAllUsers();

        assertEquals(2, result.size());
    }

    @Test
    void updateUser_ShouldUpdateUser_WhenUserExists() {
        User user = new User();
        user.setName("Old Name");
        user.setEmail("old@example.com");
        user = userRepository.save(user);

        UserDto updateDto = new UserDto();
        updateDto.setName("New Name");
        updateDto.setEmail("new@example.com");

        UserDto result = userService.updateUser(user.getId(), updateDto);

        assertEquals("New Name", result.getName());
        assertEquals("new@example.com", result.getEmail());

        // Проверяем, что пользователь в базе обновлен
        User updatedUser = userRepository.findById(user.getId()).orElse(null);
        assertNotNull(updatedUser);
        assertEquals("New Name", updatedUser.getName());
        assertEquals("new@example.com", updatedUser.getEmail());
    }
}