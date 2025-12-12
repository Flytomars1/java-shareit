package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ItemServiceIntegrationTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;  // Добавлено

    @Autowired
    private CommentRepository commentRepository;  // Добавлено

    private ItemServiceImpl itemService;

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

        itemService = new ItemServiceImpl(itemRepository, mockUserService, bookingRepository, commentRepository, userRepository);

        // Создаем тестового пользователя
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser = userRepository.save(testUser);
    }

    @Test
    void create_ShouldSaveItem_WhenValidInput() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);

        ItemDto result = itemService.create(itemDto, testUser.getId());

        assertNotNull(result.getId());
        assertEquals("Test Item", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertTrue(result.getAvailable());

        // Проверяем, что элемент действительно сохранился в базе
        List<Item> itemsInDb = itemRepository.findByOwnerIdOrderById(testUser.getId());
        assertEquals(1, itemsInDb.size());
        assertEquals("Test Item", itemsInDb.get(0).getName());
    }

    @Test
    void create_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Test Item");
        itemDto.setDescription("Test Description");
        itemDto.setAvailable(true);

        assertThrows(NotFoundException.class, () -> itemService.create(itemDto, 999L));
    }

    @Test
    void update_ShouldUpdateItem_WhenUserIsOwner() {
        // Создаем элемент через репозиторий
        Item item = new Item();
        item.setName("Old Name");
        item.setDescription("Old Description");
        item.setAvailable(true);
        item.setOwnerId(testUser.getId());
        item = itemRepository.save(item);

        ItemDto updateDto = new ItemDto();
        updateDto.setName("New Name");
        updateDto.setDescription("New Description");
        updateDto.setAvailable(false);

        ItemDto result = itemService.update(item.getId(), updateDto, testUser.getId());

        assertEquals("New Name", result.getName());
        assertEquals("New Description", result.getDescription());
        assertFalse(result.getAvailable());

        // Проверяем, что элемент в базе обновлен
        Item updatedItem = itemRepository.findById(item.getId()).orElse(null);
        assertNotNull(updatedItem);
        assertEquals("New Name", updatedItem.getName());
    }

    @Test
    void getOwnerItems_ShouldReturnItems_WhenUserIsOwner() {
        Item item1 = new Item();
        item1.setName("Item 1");
        item1.setDescription("Description 1");
        item1.setOwnerId(testUser.getId());
        item1.setAvailable(true);
        itemRepository.save(item1);

        Item item2 = new Item();
        item2.setName("Item 2");
        item2.setDescription("Description 2");
        item2.setOwnerId(testUser.getId());
        item2.setAvailable(false);
        itemRepository.save(item2);

        List<ItemDto> result = itemService.getOwnerItems(testUser.getId());

        assertEquals(2, result.size());
        assertEquals("Item 1", result.get(0).getName());
        assertEquals("Item 2", result.get(1).getName());
    }

    @Test
    void search_ShouldReturnItems_WhenTextMatches() {
        Item item1 = new Item();
        item1.setName("Searchable Item");
        item1.setDescription("This is a searchable item");
        item1.setOwnerId(testUser.getId());
        item1.setAvailable(true);
        itemRepository.save(item1);

        Item item2 = new Item();
        item2.setName("Another Item");
        item2.setDescription("This is another item");
        item2.setOwnerId(testUser.getId());
        item2.setAvailable(true);
        itemRepository.save(item2);

        List<ItemDto> result = itemService.search("Searchable");

        assertEquals(1, result.size());
        assertEquals("Searchable Item", result.get(0).getName());
    }
}