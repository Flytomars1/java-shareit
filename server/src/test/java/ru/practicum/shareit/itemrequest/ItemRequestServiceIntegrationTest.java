package ru.practicum.shareit.itemrequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.itemrequest.dto.ItemRequestDto;
import ru.practicum.shareit.itemrequest.model.ItemRequest;
import ru.practicum.shareit.itemrequest.repository.ItemRequestRepository;
import ru.practicum.shareit.itemrequest.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    private ItemRequestServiceImpl itemRequestService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Создаем мок UserService
        UserService mockUserService = new UserService() {
            @Override
            public List<UserDto> getAllUsers() {
                return List.of();
            }

            @Override
            public UserDto getUserById(Long id) {
                return null;
            }

            @Override
            public UserDto createUser(UserDto userDto) {
                return null;
            }

            @Override
            public UserDto updateUser(Long id, UserDto userDto) {
                return null;
            }

            @Override
            public void deleteUser(Long id) {
            }

            @Override
            public boolean userExists(Long userId) {
                return userRepository.existsById(userId);
            }
        };

        itemRequestService = new ItemRequestServiceImpl(itemRequestRepository, null, mockUserService);

        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser = userRepository.save(testUser);
    }

    @Test
    void create_ShouldSaveItemRequest_WhenValidInput() {
        String description = "Request for test item";

        ItemRequestDto result = itemRequestService.create(testUser.getId(), description);

        assertNotNull(result.getId());
        assertEquals(description, result.getDescription());

        // Проверяем, что запрос действительно сохранился в базе
        List<ItemRequest> requestsInDb = itemRequestRepository.findByRequesterIdOrderByIdDesc(testUser.getId());
        assertEquals(1, requestsInDb.size());
        assertEquals(description, requestsInDb.get(0).getDescription());
    }

    @Test
    void create_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        String description = "Request for test item";

        assertThrows(NotFoundException.class, () -> itemRequestService.create(999L, description));
    }
}