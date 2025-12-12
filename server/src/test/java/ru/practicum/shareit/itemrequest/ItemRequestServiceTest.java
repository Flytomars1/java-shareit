package ru.practicum.shareit.itemrequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.itemrequest.dto.ItemRequestDto;
import ru.practicum.shareit.itemrequest.model.ItemRequest;
import ru.practicum.shareit.itemrequest.repository.ItemRequestRepository;
import ru.practicum.shareit.itemrequest.service.ItemRequestServiceImpl;
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
class ItemRequestServiceTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserService userService;

    private ItemRequestServiceImpl itemRequestService;

    @BeforeEach
    void setUp() {
        itemRequestService = new ItemRequestServiceImpl(itemRequestRepository, itemRepository, userService);
    }

    @Test
    void create_ShouldReturnItemRequestDto_WhenUserExists() {
        Long userId = 1L;
        String description = "Request description";

        when(userService.userExists(userId)).thenReturn(true);

        ItemRequest savedRequest = new ItemRequest();
        savedRequest.setId(1L);
        savedRequest.setDescription(description);
        savedRequest.setRequesterId(userId);
        savedRequest.setCreated(LocalDateTime.now());

        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(savedRequest);

        ItemRequestDto result = itemRequestService.create(userId, description);

        assertEquals(description, result.getDescription());
        verify(userService).userExists(userId);
        verify(itemRequestRepository).save(any(ItemRequest.class));
    }

    @Test
    void create_ShouldThrowValidationException_WhenDescriptionIsNull() {
        Long userId = 1L;
        String description = null;

        assertThrows(ValidationException.class, () -> itemRequestService.create(userId, description));
        verify(userService, never()).userExists(anyLong());
        verify(itemRequestRepository, never()).save(any(ItemRequest.class));
    }

    @Test
    void create_ShouldThrowValidationException_WhenDescriptionIsBlank() {
        Long userId = 1L;
        String description = "   ";

        assertThrows(ValidationException.class, () -> itemRequestService.create(userId, description));
        verify(userService, never()).userExists(anyLong());
        verify(itemRequestRepository, never()).save(any(ItemRequest.class));
    }

    @Test
    void create_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        Long userId = 1L;
        String description = "Request description";

        when(userService.userExists(userId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> itemRequestService.create(userId, description));
        verify(userService).userExists(userId);
        verify(itemRequestRepository, never()).save(any(ItemRequest.class));
    }

    @Test
    void getOwnRequests_ShouldReturnListOfItemRequestDtos_WhenUserExists() {
        Long userId = 1L;

        when(userService.userExists(userId)).thenReturn(true);

        ItemRequest request1 = new ItemRequest();
        request1.setId(1L);
        request1.setDescription("Request 1");
        request1.setRequesterId(userId);

        ItemRequest request2 = new ItemRequest();
        request2.setId(2L);
        request2.setDescription("Request 2");
        request2.setRequesterId(userId);

        List<ItemRequest> requests = Arrays.asList(request1, request2);
        when(itemRequestRepository.findByRequesterIdOrderByIdDesc(userId)).thenReturn(requests);

        when(itemRepository.findByRequestIdOrderById(1L)).thenReturn(List.of());
        when(itemRepository.findByRequestIdOrderById(2L)).thenReturn(List.of());

        List<ItemRequestDto> result = itemRequestService.getOwnRequests(userId);

        assertEquals(2, result.size());
        verify(userService).userExists(userId);
        verify(itemRequestRepository).findByRequesterIdOrderByIdDesc(userId);
    }

    @Test
    void getOwnRequests_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        Long userId = 1L;

        when(userService.userExists(userId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> itemRequestService.getOwnRequests(userId));
        verify(userService).userExists(userId);
        verify(itemRequestRepository, never()).findByRequesterIdOrderByIdDesc(anyLong());
    }

    @Test
    void getAllRequests_ShouldReturnListOfItemRequestDtos_WhenUserExists() {
        Long userId = 1L;

        when(userService.userExists(userId)).thenReturn(true);

        ItemRequest request1 = new ItemRequest();
        request1.setId(1L);
        request1.setDescription("Request 1");
        request1.setRequesterId(2L);

        ItemRequest request2 = new ItemRequest();
        request2.setId(2L);
        request2.setDescription("Request 2");
        request2.setRequesterId(3L);

        List<ItemRequest> requests = Arrays.asList(request1, request2);
        when(itemRequestRepository.findByRequesterIdNotOrderByIdDesc(userId)).thenReturn(requests);

        when(itemRepository.findByRequestIdOrderById(1L)).thenReturn(List.of());
        when(itemRepository.findByRequestIdOrderById(2L)).thenReturn(List.of());

        List<ItemRequestDto> result = itemRequestService.getAllRequests(userId, 0, 20);

        assertEquals(2, result.size());
        verify(userService).userExists(userId);
        verify(itemRequestRepository).findByRequesterIdNotOrderByIdDesc(userId);
    }

    @Test
    void getAllRequests_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        Long userId = 1L;

        when(userService.userExists(userId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> itemRequestService.getAllRequests(userId, 0, 20));
        verify(userService).userExists(userId);
        verify(itemRequestRepository, never()).findByRequesterIdNotOrderByIdDesc(anyLong());
    }

    @Test
    void getRequestById_ShouldReturnItemRequestDto_WhenRequestExists() {
        Long requestId = 1L;
        Long userId = 1L;

        when(userService.userExists(userId)).thenReturn(true);

        ItemRequest request = new ItemRequest();
        request.setId(requestId);
        request.setDescription("Request description");
        request.setRequesterId(2L);

        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(itemRepository.findByRequestIdOrderById(requestId)).thenReturn(List.of());

        ItemRequestDto result = itemRequestService.getRequestById(requestId, userId);

        assertEquals("Request description", result.getDescription());
        verify(userService).userExists(userId);
        verify(itemRequestRepository).findById(requestId);
    }

    @Test
    void getRequestById_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        Long requestId = 1L;
        Long userId = 1L;

        when(userService.userExists(userId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> itemRequestService.getRequestById(requestId, userId));
        verify(userService).userExists(userId);
        verify(itemRequestRepository, never()).findById(anyLong());
    }

    @Test
    void getRequestById_ShouldThrowNotFoundException_WhenRequestDoesNotExist() {
        Long requestId = 1L;
        Long userId = 1L;

        when(userService.userExists(userId)).thenReturn(true);
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.getRequestById(requestId, userId));
        verify(userService).userExists(userId);
        verify(itemRequestRepository).findById(requestId);
    }

    @Test
    void getAllRequests_ShouldReturnAllRequests_WhenUserExists() {
        Long userId = 1L;

        ItemRequest request1 = new ItemRequest();
        request1.setId(1L);
        request1.setDescription("Request 1");
        request1.setRequesterId(2L);

        ItemRequest request2 = new ItemRequest();
        request2.setId(2L);
        request2.setDescription("Request 2");
        request2.setRequesterId(3L);

        when(userService.userExists(userId)).thenReturn(true);
        when(itemRequestRepository.findByRequesterIdNotOrderByIdDesc(userId)).thenReturn(List.of(request1, request2));
        when(itemRepository.findByRequestIdOrderById(1L)).thenReturn(List.of());
        when(itemRepository.findByRequestIdOrderById(2L)).thenReturn(List.of());

        List<ItemRequestDto> result = itemRequestService.getAllRequests(userId, 0, 20);

        assertEquals(2, result.size());
        verify(userService).userExists(userId);
        verify(itemRequestRepository).findByRequesterIdNotOrderByIdDesc(userId);
    }

    @Test
    void getRequestById_ShouldReturnRequest_WhenUserExists() {
        Long requestId = 1L;
        Long userId = 1L;

        ItemRequest request = new ItemRequest();
        request.setId(requestId);
        request.setDescription("Request description");
        request.setRequesterId(2L);

        when(userService.userExists(userId)).thenReturn(true);
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(itemRepository.findByRequestIdOrderById(requestId)).thenReturn(List.of());

        ItemRequestDto result = itemRequestService.getRequestById(requestId, userId);

        assertEquals("Request description", result.getDescription());
        verify(userService).userExists(userId);
        verify(itemRequestRepository).findById(requestId);
    }
}