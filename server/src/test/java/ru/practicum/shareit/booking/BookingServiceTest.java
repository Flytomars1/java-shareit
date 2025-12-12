package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    private BookingServiceImpl bookingService;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(bookingRepository, userRepository, itemRepository);
    }

    @Test
    void create_ShouldReturnBookingDto_WhenValidInput() {
        Long userId = 1L;
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setStart(LocalDateTime.now().plusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(2));
        bookingDto.setItemId(1L);

        User booker = new User();
        booker.setId(userId);

        Item item = new Item();
        item.setId(1L);
        item.setOwnerId(2L);
        item.setAvailable(true);

        Booking savedBooking = BookingMapper.toBooking(bookingDto, userId);
        savedBooking.setId(1L);
        savedBooking.setStatus(Status.WAITING);

        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        BookingDto result = bookingService.create(bookingDto, userId);

        assertEquals(Status.WAITING, result.getStatus());
        verify(userRepository).findById(userId);
        verify(itemRepository).findById(1L);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void create_ShouldThrowValidationException_WhenStartIsNull() {
        Long userId = 1L;
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setStart(null);
        bookingDto.setEnd(LocalDateTime.now().plusHours(2));
        bookingDto.setItemId(1L);

        assertThrows(ValidationException.class, () -> bookingService.create(bookingDto, userId));
        verify(userRepository, never()).findById(anyLong());
        verify(itemRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void create_ShouldThrowValidationException_WhenEndIsNull() {
        Long userId = 1L;
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setStart(LocalDateTime.now().plusHours(1));
        bookingDto.setEnd(null);
        bookingDto.setItemId(1L);

        assertThrows(ValidationException.class, () -> bookingService.create(bookingDto, userId));
        verify(userRepository, never()).findById(anyLong());
        verify(itemRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void create_ShouldThrowValidationException_WhenEndBeforeStart() {
        Long userId = 1L;
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setStart(LocalDateTime.now().plusHours(2));
        bookingDto.setEnd(LocalDateTime.now().plusHours(1));
        bookingDto.setItemId(1L);

        assertThrows(ValidationException.class, () -> bookingService.create(bookingDto, userId));
        verify(userRepository, never()).findById(anyLong());
        verify(itemRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void create_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        Long userId = 1L;
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setStart(LocalDateTime.now().plusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(2));
        bookingDto.setItemId(1L);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.create(bookingDto, userId));
        verify(userRepository).findById(userId);
        verify(itemRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void create_ShouldThrowNotFoundException_WhenItemDoesNotExist() {
        Long userId = 1L;
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setStart(LocalDateTime.now().plusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(2));
        bookingDto.setItemId(1L);

        User booker = new User();
        booker.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.create(bookingDto, userId));
        verify(userRepository).findById(userId);
        verify(itemRepository).findById(1L);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void create_ShouldThrowValidationException_WhenItemNotAvailable() {
        Long userId = 1L;
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setStart(LocalDateTime.now().plusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(2));
        bookingDto.setItemId(1L);

        User booker = new User();
        booker.setId(userId);

        Item item = new Item();
        item.setId(1L);
        item.setOwnerId(2L);
        item.setAvailable(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () -> bookingService.create(bookingDto, userId));
        verify(userRepository).findById(userId);
        verify(itemRepository).findById(1L);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void create_ShouldThrowValidationException_WhenBookingOwnItem() {
        Long userId = 1L;
        Long ownerId = 1L;
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setStart(LocalDateTime.now().plusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(2));
        bookingDto.setItemId(1L);

        User booker = new User();
        booker.setId(userId);

        Item item = new Item();
        item.setId(1L);
        item.setOwnerId(ownerId);
        item.setAvailable(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () -> bookingService.create(bookingDto, userId));
        verify(userRepository).findById(userId);
        verify(itemRepository).findById(1L);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void approve_ShouldReturnApprovedBookingDto_WhenValidInput() {
        Long bookingId = 1L;
        Long userId = 2L;
        Boolean approved = true;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setItemId(1L);
        booking.setBookerId(1L);
        booking.setStatus(Status.WAITING);

        Item item = new Item();
        item.setId(1L);
        item.setOwnerId(userId);

        User booker = new User();
        booker.setId(1L);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(1L)).thenReturn(Optional.of(booker));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingDto result = bookingService.approve(bookingId, userId, approved);

        assertEquals(Status.APPROVED, result.getStatus());
        verify(bookingRepository).findById(bookingId);
        verify(itemRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void approve_ShouldThrowNotFoundException_WhenBookingDoesNotExist() {
        Long bookingId = 1L;
        Long userId = 2L;
        Boolean approved = true;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.approve(bookingId, userId, approved));
        verify(bookingRepository).findById(bookingId);
        verify(itemRepository, never()).findById(anyLong());
        verify(userRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void approve_ShouldThrowAccessDeniedException_WhenUserIsNotOwner() {
        Long bookingId = 1L;
        Long userId = 3L;
        Boolean approved = true;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setItemId(1L);
        booking.setBookerId(1L);
        booking.setStatus(Status.WAITING);

        Item item = new Item();
        item.setId(1L);
        item.setOwnerId(2L);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(AccessDeniedException.class, () -> bookingService.approve(bookingId, userId, approved));
        verify(bookingRepository).findById(bookingId);
        verify(itemRepository).findById(1L);
        verify(userRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void approve_ShouldThrowValidationException_WhenBookingStatusIsNotWaiting() {
        Long bookingId = 1L;
        Long userId = 2L;
        Boolean approved = true;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setItemId(1L);
        booking.setBookerId(1L);
        booking.setStatus(Status.APPROVED);

        Item item = new Item();
        item.setId(1L);
        item.setOwnerId(userId);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () -> bookingService.approve(bookingId, userId, approved));
        verify(bookingRepository).findById(bookingId);
        verify(itemRepository).findById(1L);
        verify(userRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void getById_ShouldReturnBookingDto_WhenUserIsBooker() {
        Long bookingId = 1L;
        Long userId = 1L;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setItemId(1L);
        booking.setBookerId(userId);
        booking.setStatus(Status.WAITING);

        Item item = new Item();
        item.setId(1L);

        User booker = new User();
        booker.setId(userId);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));

        BookingDto result = bookingService.getById(bookingId, userId);

        assertEquals(bookingId, result.getId());
        verify(bookingRepository).findById(bookingId);
        verify(itemRepository).findById(1L);
        verify(userRepository).findById(userId);
    }

    @Test
    void getById_ShouldReturnBookingDto_WhenUserIsOwner() {
        Long bookingId = 1L;
        Long userId = 2L;
        Long bookerId = 1L;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setItemId(1L);
        booking.setBookerId(bookerId);
        booking.setStatus(Status.WAITING);

        Item item = new Item();
        item.setId(1L);
        item.setOwnerId(userId);

        User booker = new User();
        booker.setId(bookerId);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));

        BookingDto result = bookingService.getById(bookingId, userId);

        assertEquals(bookingId, result.getId());
        verify(bookingRepository).findById(bookingId);
        verify(itemRepository).findById(1L);
        verify(userRepository).findById(bookerId);
    }

    @Test
    void getById_ShouldThrowAccessDeniedException_WhenUserIsNotBookerOrOwner() {
        Long bookingId = 1L;
        Long userId = 3L;
        Long bookerId = 1L;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setItemId(1L);
        booking.setBookerId(bookerId);
        booking.setStatus(Status.WAITING);

        Item item = new Item();
        item.setId(1L);
        item.setOwnerId(2L);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(AccessDeniedException.class, () -> bookingService.getById(bookingId, userId));
        verify(bookingRepository).findById(bookingId);
        verify(itemRepository).findById(1L);
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void validateBookingDates_ShouldThrowValidationException_WhenStartIsInPast() {
        Long userId = 1L;
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setStart(LocalDateTime.now().minusMinutes(2));
        bookingDto.setEnd(LocalDateTime.now().plusHours(1));
        bookingDto.setItemId(1L);

        assertThrows(ValidationException.class, () -> bookingService.create(bookingDto, userId));
        verify(userRepository, never()).findById(anyLong());
        verify(itemRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }
}