package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserService userService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    private ItemServiceImpl itemService;

    @BeforeEach
    void setUp() {
        itemService = new ItemServiceImpl(itemRepository, userService, bookingRepository, commentRepository, userRepository);
    }

    @Test
    void create_ShouldReturnItemDto_WhenUserExists() {
        Long userId = 1L;
        ItemDto inputDto = new ItemDto();
        inputDto.setName("Item Name");
        inputDto.setDescription("Item Description");
        inputDto.setAvailable(true);

        Item savedItem = ItemMapper.toItem(inputDto, userId);
        savedItem.setId(1L);

        when(userService.userExists(userId)).thenReturn(true);
        when(itemRepository.save(any(Item.class))).thenReturn(savedItem);

        ItemDto result = itemService.create(inputDto, userId);

        assertEquals("Item Name", result.getName());
        assertEquals("Item Description", result.getDescription());
        assertTrue(result.getAvailable());
        verify(userService).userExists(userId);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void create_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        Long userId = 1L;
        ItemDto inputDto = new ItemDto();
        inputDto.setName("Item Name");
        inputDto.setDescription("Item Description");
        inputDto.setAvailable(true);

        when(userService.userExists(userId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> itemService.create(inputDto, userId));
        verify(userService).userExists(userId);
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void update_ShouldReturnUpdatedItemDto_WhenUserIsOwner() {
        Long itemId = 1L;
        Long userId = 1L;
        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setName("Old Name");
        existingItem.setDescription("Old Description");
        existingItem.setAvailable(true);
        existingItem.setOwnerId(userId);

        ItemDto updateDto = new ItemDto();
        updateDto.setName("New Name");
        updateDto.setDescription("New Description");
        updateDto.setAvailable(false);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(any(Item.class))).thenReturn(existingItem);

        ItemDto result = itemService.update(itemId, updateDto, userId);

        assertEquals("New Name", result.getName());
        assertEquals("New Description", result.getDescription());
        assertFalse(result.getAvailable());
        verify(itemRepository).findById(itemId);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void update_ShouldThrowNotFoundException_WhenItemDoesNotExist() {
        Long itemId = 1L;
        Long userId = 1L;
        ItemDto updateDto = new ItemDto();
        updateDto.setName("New Name");

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.update(itemId, updateDto, userId));
        verify(itemRepository).findById(itemId);
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void update_ShouldThrowNotFoundException_WhenUserIsNotOwner() {
        Long itemId = 1L;
        Long userId = 1L;
        Long ownerId = 2L;
        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setOwnerId(ownerId);

        ItemDto updateDto = new ItemDto();
        updateDto.setName("New Name");

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));

        assertThrows(NotFoundException.class, () -> itemService.update(itemId, updateDto, userId));
        verify(itemRepository).findById(itemId);
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void findById_ShouldReturnItemDto_WhenItemExists() {
        Long itemId = 1L;
        Item item = new Item();
        item.setId(itemId);
        item.setName("Item Name");
        item.setOwnerId(1L);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findLastBookingsByItemIdAndStatus(anyLong(), any(), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(bookingRepository.findNextBookingsByItemIdAndStatus(anyLong(), any(), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(commentRepository.findCommentDtosByItemId(anyLong())).thenReturn(List.of());


        ItemDto result = itemService.findById(itemId, 1L);

        assertEquals("Item Name", result.getName());
        verify(itemRepository).findById(itemId);
    }

    @Test
    void findById_ShouldThrowNotFoundException_WhenItemDoesNotExist() {
        Long itemId = 1L;

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.findById(itemId, 1L));
        verify(itemRepository).findById(itemId);
    }

    @Test
    void search_ShouldReturnEmptyList_WhenTextIsNull() {
        List<ItemDto> result = itemService.search(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void search_ShouldReturnEmptyList_WhenTextIsBlank() {
        List<ItemDto> result = itemService.search("   ");

        assertTrue(result.isEmpty());
    }

    @Test
    void search_ShouldReturnItemList_WhenTextIsValid() {
        String text = "search";
        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("Search Item");

        when(itemRepository.searchByText(text)).thenReturn(Arrays.asList(item1));

        List<ItemDto> result = itemService.search(text);

        assertEquals(1, result.size());
        assertEquals("Search Item", result.get(0).getName());
        verify(itemRepository).searchByText(text);
    }

    @Test
    void getOwnerItems_ShouldReturnItemList_WhenUserIsOwner() {
        Long userId = 1L;
        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("Item 1");
        item1.setOwnerId(userId);

        when(itemRepository.findByOwnerIdOrderById(userId)).thenReturn(Arrays.asList(item1));
        when(bookingRepository.findLastBookingsByItemIdAndStatus(anyLong(), any(), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(bookingRepository.findNextBookingsByItemIdAndStatus(anyLong(), any(), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(commentRepository.findCommentDtosByItemId(anyLong())).thenReturn(List.of());

        List<ItemDto> result = itemService.getOwnerItems(userId);

        assertEquals(1, result.size());
        assertEquals("Item 1", result.get(0).getName());
        verify(itemRepository).findByOwnerIdOrderById(userId);
    }

    @Test
    void addComment_ShouldReturnCommentDto_WhenUserHasFinishedBooking() {
        Long itemId = 1L;
        Long userId = 1L;
        String text = "Great item!";

        Item item = new Item();
        item.setId(itemId);
        item.setOwnerId(2L);

        User author = new User();
        author.setId(userId);
        author.setName("Author Name");

        Comment savedComment = new Comment();
        savedComment.setId(1L);
        savedComment.setText(text);
        savedComment.setCreated(LocalDateTime.now());

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBookerId(userId);
        booking.setItemId(itemId);
        booking.setStatus(Status.APPROVED);
        booking.setEnd(LocalDateTime.now().minusHours(1)); // Завершено

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(anyLong(), anyLong(), any(), any(LocalDateTime.class)))
                .thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(author));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        CommentDto result = itemService.addComment(itemId, userId, text);

        assertEquals(text, result.getText());
        assertEquals("Author Name", result.getAuthorName());
        verify(itemRepository).findById(itemId);
        verify(bookingRepository).existsByBookerIdAndItemIdAndStatusAndEndBefore(anyLong(), anyLong(), any(), any(LocalDateTime.class));
        verify(userRepository).findById(userId);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void addComment_ShouldThrowValidationException_WhenTextIsNull() {
        Long itemId = 1L;
        Long userId = 1L;
        String text = null;

        assertThrows(ValidationException.class, () -> itemService.addComment(itemId, userId, text));
        verify(itemRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).existsByBookerIdAndItemIdAndStatusAndEndBefore(anyLong(), anyLong(), any(), any(LocalDateTime.class));
    }

    @Test
    void addComment_ShouldThrowValidationException_WhenTextIsBlank() {
        Long itemId = 1L;
        Long userId = 1L;
        String text = "   ";

        assertThrows(ValidationException.class, () -> itemService.addComment(itemId, userId, text));
        verify(itemRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).existsByBookerIdAndItemIdAndStatusAndEndBefore(anyLong(), anyLong(), any(), any(LocalDateTime.class));
    }

    @Test
    void addComment_ShouldThrowNotFoundException_WhenItemDoesNotExist() {
        Long itemId = 1L;
        Long userId = 1L;
        String text = "Great item!";

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.addComment(itemId, userId, text));
        verify(itemRepository).findById(itemId);
        verify(bookingRepository, never()).existsByBookerIdAndItemIdAndStatusAndEndBefore(anyLong(), anyLong(), any(), any(LocalDateTime.class));
    }

    @Test
    void addComment_ShouldThrowValidationException_WhenUserHasNoFinishedBooking() {
        Long itemId = 1L;
        Long userId = 1L;
        String text = "Great item!";
        Item item = new Item();
        item.setId(itemId);
        item.setOwnerId(2L);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(anyLong(), anyLong(), any(), any(LocalDateTime.class)))
                .thenReturn(false);

        assertThrows(ValidationException.class, () -> itemService.addComment(itemId, userId, text));
        verify(itemRepository).findById(itemId);
        verify(bookingRepository).existsByBookerIdAndItemIdAndStatusAndEndBefore(anyLong(), anyLong(), any(), any(LocalDateTime.class));
    }

    @Test
    void create_ShouldThrowValidationException_WhenItemNameIsNull() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName(null);
        itemDto.setDescription("Test description");
        itemDto.setAvailable(true);

        assertThrows(ValidationException.class, () -> itemService.create(itemDto, 1L));
    }

    @Test
    void create_ShouldThrowValidationException_WhenItemNameIsBlank() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("   ");
        itemDto.setDescription("Test description");
        itemDto.setAvailable(true);

        assertThrows(ValidationException.class, () -> itemService.create(itemDto, 1L));
    }

    @Test
    void create_ShouldThrowValidationException_WhenItemDescriptionIsNull() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Test name");
        itemDto.setDescription(null);
        itemDto.setAvailable(true);

        assertThrows(ValidationException.class, () -> itemService.create(itemDto, 1L));
    }

    @Test
    void create_ShouldThrowValidationException_WhenItemAvailableIsNull() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Test name");
        itemDto.setDescription("Test description");
        itemDto.setAvailable(null);

        assertThrows(ValidationException.class, () -> itemService.create(itemDto, 1L));
    }

    @Test
    void update_ShouldUpdateItem_WhenItemNameIsNotBlank() {
        Long itemId = 1L;
        Long userId = 1L;

        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setOwnerId(userId);
        existingItem.setName("Old Name");
        existingItem.setDescription("Old Description");
        existingItem.setAvailable(true);

        ItemDto updateDto = new ItemDto();
        updateDto.setName("New Name");

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(any(Item.class))).thenReturn(existingItem);

        ItemDto result = itemService.update(itemId, updateDto, userId);

        assertEquals("New Name", result.getName());
        verify(itemRepository).findById(itemId);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void create_ShouldThrowValidationException_WhenItemDescriptionIsBlank() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Test Name");
        itemDto.setDescription("   ");
        itemDto.setAvailable(true);

        assertThrows(ValidationException.class, () -> itemService.create(itemDto, 1L));

        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void update_ShouldUpdateItem_WhenAvailableIsNull() {
        Long itemId = 1L;
        Long userId = 1L;

        Item existingItem = new Item();
        existingItem.setId(itemId);
        existingItem.setOwnerId(userId);
        existingItem.setName("Old Name");
        existingItem.setDescription("Old Description");
        existingItem.setAvailable(true);

        ItemDto updateDto = new ItemDto();
        updateDto.setAvailable(null);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(existingItem));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemDto result = itemService.update(itemId, updateDto, userId);

        assertEquals(true, result.getAvailable());
        verify(itemRepository).findById(itemId);
        verify(itemRepository).save(any(Item.class));
    }
}