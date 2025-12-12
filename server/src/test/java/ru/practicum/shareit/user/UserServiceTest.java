package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void createUser_ShouldReturnUserDto_WhenEmailIsUnique() {
        UserDto inputDto = new UserDto(null, "John Doe", "john@example.com");
        User savedUser = UserMapper.toUser(inputDto);
        savedUser.setId(1L);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDto result = userService.createUser(inputDto);

        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_ShouldThrowValidationException_WhenEmailIsNull() {
        UserDto inputDto = new UserDto(null, "John Doe", null);

        assertThrows(ValidationException.class, () -> userService.createUser(inputDto));
    }

    @Test
    void createUser_ShouldThrowValidationException_WhenEmailIsInvalid() {
        UserDto inputDto = new UserDto(null, "John Doe", "invalid-email");

        assertThrows(ValidationException.class, () -> userService.createUser(inputDto));
    }

    @Test
    void createUser_ShouldThrowEmailAlreadyExistsException_WhenEmailAlreadyExists() {
        UserDto inputDto = new UserDto(null, "John Doe", "john@example.com");

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.createUser(inputDto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_ShouldReturnUserDto_WhenUserExists() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setName("John Doe");
        user.setEmail("john@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserDto result = userService.getUserById(userId);

        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserById(userId));
        verify(userRepository).findById(userId);
    }

    @Test
    void updateUser_ShouldReturnUpdatedUserDto_WhenUserExists() {
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Old Name");
        existingUser.setEmail("old@example.com");

        UserDto updateDto = new UserDto(null, "New Name", "new@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        UserDto result = userService.updateUser(userId, updateDto);

        assertEquals("New Name", result.getName());
        assertEquals("new@example.com", result.getEmail());
        verify(userRepository).findById(userId);
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateUser_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        Long userId = 1L;
        UserDto updateDto = new UserDto(null, "New Name", "new@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.updateUser(userId, updateDto));
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_ShouldThrowValidationException_WhenEmailIsBlank() {
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Old Name");
        existingUser.setEmail("old@example.com");

        UserDto updateDto = new UserDto(null, "New Name", "   ");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

        assertThrows(ValidationException.class, () -> userService.updateUser(userId, updateDto));
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_ShouldThrowEmailAlreadyExistsException_WhenNewEmailAlreadyExists() {
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Old Name");
        existingUser.setEmail("old@example.com");

        UserDto updateDto = new UserDto(null, "New Name", "existing@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.updateUser(userId, updateDto));
        verify(userRepository).findById(userId);
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getAllUsers_ShouldReturnListOfUserDtos() {
        User user1 = new User();
        user1.setId(1L);
        user1.setName("John Doe");
        user1.setEmail("john@example.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setName("Jane Doe");
        user2.setEmail("jane@example.com");

        List<User> users = Arrays.asList(user1, user2);

        when(userRepository.findAll()).thenReturn(users);

        List<UserDto> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getName());
        assertEquals("jane@example.com", result.get(1).getEmail());
        verify(userRepository).findAll();
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenUserExists() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);

        userService.deleteUser(userId);

        verify(userRepository).existsById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> userService.deleteUser(userId));
        verify(userRepository).existsById(userId);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void userExists_ShouldReturnTrue_WhenUserExists() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(true);

        boolean result = userService.userExists(userId);

        assertTrue(result);
        verify(userRepository).existsById(userId);
    }

    @Test
    void userExists_ShouldReturnFalse_WhenUserDoesNotExist() {
        Long userId = 1L;
        when(userRepository.existsById(userId)).thenReturn(false);

        boolean result = userService.userExists(userId);

        assertFalse(result);
        verify(userRepository).existsById(userId);
    }
}