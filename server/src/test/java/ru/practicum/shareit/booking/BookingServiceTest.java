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

    @Test
    void getUserBookings_ShouldReturnAllBookings_WhenStateIsAll() {
        Long userId = 1L;  // бронирующий пользователь
        String state = "ALL";

        Booking booking1 = new Booking();
        booking1.setId(1L);
        booking1.setBookerId(userId);  // бронирующий пользователь
        booking1.setItemId(1L);
        booking1.setStatus(Status.WAITING);

        Booking booking2 = new Booking();
        booking2.setId(2L);
        booking2.setBookerId(userId);  // бронирующий пользователь
        booking2.setItemId(1L);
        booking2.setStatus(Status.APPROVED);

        Item item = new Item();
        item.setId(1L);

        User booker = new User();
        booker.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdOrderByIdDesc(userId)).thenReturn(List.of(booking1, booking2));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));  // для toBookingDto

        List<BookingDto> result = bookingService.getUserBookings(userId, state);

        assertEquals(2, result.size());
        verify(bookingRepository).findByBookerIdOrderByIdDesc(userId);
    }

    @Test
    void getUserBookings_ShouldReturnCurrentBookings_WhenStateIsCurrent() {
        Long userId = 1L;
        String state = "CURRENT";

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBookerId(userId);
        booking.setItemId(1L);
        booking.setStart(LocalDateTime.now().minusHours(1));
        booking.setEnd(LocalDateTime.now().plusHours(1));
        booking.setStatus(Status.APPROVED);

        Item item = new Item();
        item.setId(1L);

        User booker = new User();
        booker.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findCurrentBookingsByBooker(eq(userId), any(LocalDateTime.class))).thenReturn(List.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));

        List<BookingDto> result = bookingService.getUserBookings(userId, state);

        assertEquals(1, result.size());
        verify(bookingRepository).findCurrentBookingsByBooker(eq(userId), any(LocalDateTime.class));
    }

    @Test
    void getUserBookings_ShouldReturnPastBookings_WhenStateIsPast() {
        Long userId = 1L;
        String state = "PAST";
        LocalDateTime now = LocalDateTime.now();

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBookerId(userId);
        booking.setItemId(1L);
        booking.setStart(now.minusHours(2));
        booking.setEnd(now.minusHours(1));
        booking.setStatus(Status.APPROVED);

        Item item = new Item();
        item.setId(1L);

        User booker = new User();
        booker.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findPastBookingsByBooker(eq(userId), any(LocalDateTime.class))).thenReturn(List.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));

        List<BookingDto> result = bookingService.getUserBookings(userId, state);

        assertEquals(1, result.size());
        verify(bookingRepository).findPastBookingsByBooker(eq(userId), any(LocalDateTime.class));
    }

    @Test
    void getUserBookings_ShouldReturnFutureBookings_WhenStateIsFuture() {
        Long userId = 1L;
        String state = "FUTURE";

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBookerId(userId);
        booking.setItemId(1L);
        booking.setStart(LocalDateTime.now().plusHours(1));
        booking.setEnd(LocalDateTime.now().plusHours(2));
        booking.setStatus(Status.WAITING);

        Item item = new Item();
        item.setId(1L);

        User booker = new User();
        booker.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findFutureBookingsByBooker(eq(userId), any(LocalDateTime.class))).thenReturn(List.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));

        List<BookingDto> result = bookingService.getUserBookings(userId, state);

        assertEquals(1, result.size());
        verify(bookingRepository).findFutureBookingsByBooker(eq(userId), any(LocalDateTime.class));
    }

    @Test
    void getUserBookings_ShouldReturnWaitingBookings_WhenStateIsWaiting() {
        Long userId = 1L;
        String state = "WAITING";
        LocalDateTime now = LocalDateTime.now();

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBookerId(userId);
        booking.setItemId(1L);
        booking.setStart(now.plusHours(1));
        booking.setEnd(now.plusHours(2));
        booking.setStatus(Status.WAITING);

        Item item = new Item();
        item.setId(1L);

        User booker = new User();
        booker.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndStatus(userId, Status.WAITING)).thenReturn(List.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));  // для toBookingDto

        List<BookingDto> result = bookingService.getUserBookings(userId, state);

        assertEquals(1, result.size());
        verify(bookingRepository).findByBookerIdAndStatus(userId, Status.WAITING);
    }

    @Test
    void getUserBookings_ShouldReturnRejectedBookings_WhenStateIsRejected() {
        Long userId = 1L;
        String state = "REJECTED";
        LocalDateTime now = LocalDateTime.now();

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBookerId(userId);
        booking.setItemId(1L);
        booking.setStart(now.plusHours(1));
        booking.setEnd(now.plusHours(2));
        booking.setStatus(Status.REJECTED);

        Item item = new Item();
        item.setId(1L);

        User booker = new User();
        booker.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdAndStatus(userId, Status.REJECTED)).thenReturn(List.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));

        List<BookingDto> result = bookingService.getUserBookings(userId, state);

        assertEquals(1, result.size());
        verify(bookingRepository).findByBookerIdAndStatus(userId, Status.REJECTED);
    }

    @Test
    void getUserBookings_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        Long userId = 1L;
        String state = "ALL";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.getUserBookings(userId, state));
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserBookings_ShouldThrowValidationException_WhenStateIsUnknown() {
        Long userId = 1L;
        String state = "UNKNOWN";

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ValidationException.class, () -> bookingService.getUserBookings(userId, state));
    }

    @Test
    void getOwnerBookings_ShouldReturnAllBookings_WhenStateIsAll() {
        Long userId = 1L;
        Long bookerId = 2L;
        String state = "ALL";

        Booking booking1 = new Booking();
        booking1.setId(1L);
        booking1.setBookerId(bookerId);
        booking1.setItemId(1L);
        booking1.setStatus(Status.WAITING);

        Booking booking2 = new Booking();
        booking2.setId(2L);
        booking2.setBookerId(bookerId);
        booking2.setItemId(1L);
        booking2.setStatus(Status.APPROVED);

        Item item = new Item();
        item.setId(1L);
        item.setOwnerId(userId);

        User owner = new User();
        owner.setId(userId);

        User booker = new User();
        booker.setId(bookerId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdOrderByIdDesc(userId)).thenReturn(List.of(booking1, booking2));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));  // добавлено

        List<BookingDto> result = bookingService.getOwnerBookings(userId, state);

        assertEquals(2, result.size());
        verify(userRepository).findById(userId);
        verify(bookingRepository).findByItemOwnerIdOrderByIdDesc(userId);
    }

    @Test
    void getOwnerBookings_ShouldReturnCurrentBookings_WhenStateIsCurrent() {
        Long userId = 1L;
        String state = "CURRENT";

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBookerId(2L);
        booking.setItemId(1L);
        booking.setStart(LocalDateTime.now().minusHours(1));
        booking.setEnd(LocalDateTime.now().plusHours(1));
        booking.setStatus(Status.APPROVED);

        Item item = new Item();
        item.setId(1L);
        item.setOwnerId(userId);

        User booker = new User();
        booker.setId(2L);

        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findCurrentBookingsByOwner(eq(userId), any(LocalDateTime.class))).thenReturn(List.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));

        List<BookingDto> result = bookingService.getOwnerBookings(userId, state);

        assertEquals(1, result.size());
        verify(bookingRepository).findCurrentBookingsByOwner(eq(userId), any(LocalDateTime.class));
    }

    @Test
    void getOwnerBookings_ShouldReturnWaitingBookings_WhenStateIsWaiting() {
        Long userId = 1L;
        String state = "WAITING";
        LocalDateTime now = LocalDateTime.now();

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBookerId(2L);
        booking.setItemId(1L);
        booking.setStart(now.plusHours(1));
        booking.setEnd(now.plusHours(2));
        booking.setStatus(Status.WAITING);

        Item item = new Item();
        item.setId(1L);
        item.setOwnerId(userId);

        User booker = new User();
        booker.setId(2L);

        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByItemOwnerIdAndStatus(userId, Status.WAITING)).thenReturn(List.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));

        List<BookingDto> result = bookingService.getOwnerBookings(userId, state);

        assertEquals(1, result.size());
        verify(userRepository).findById(userId);
        verify(bookingRepository).findByItemOwnerIdAndStatus(userId, Status.WAITING);
    }

    @Test
    void getOwnerBookings_ShouldReturnRejectedBookings_WhenStateIsRejected() {
        Long userId = 1L;
        String state = "REJECTED";
        LocalDateTime now = LocalDateTime.now();

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setBookerId(2L);
        booking.setItemId(1L);
        booking.setStart(now.plusHours(1));
        booking.setEnd(now.plusHours(2));
        booking.setStatus(Status.REJECTED);

        Item item = new Item();
        item.setId(1L);
        item.setOwnerId(userId);

        User booker = new User();
        booker.setId(2L);

        when(userRepository.findById(userId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByItemOwnerIdAndStatus(userId, Status.REJECTED)).thenReturn(List.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));

        List<BookingDto> result = bookingService.getOwnerBookings(userId, state);

        assertEquals(1, result.size());
        verify(userRepository).findById(userId);
        verify(bookingRepository).findByItemOwnerIdAndStatus(userId, Status.REJECTED);
    }

    @Test
    void getOwnerBookings_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        Long userId = 1L;
        String state = "ALL";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.getOwnerBookings(userId, state));
        verify(userRepository).findById(userId);
    }

    @Test
    void getOwnerBookings_ShouldThrowValidationException_WhenStateIsUnknown() {
        Long userId = 1L;
        String state = "UNKNOWN";

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(ValidationException.class, () -> bookingService.getOwnerBookings(userId, state));
    }

    @Test
    void validateBookingDates_ShouldThrowValidationException_WhenStartEqualsEnd() {
        Long userId = 1L;
        BookingCreateDto bookingDto = new BookingCreateDto();
        LocalDateTime sameTime = LocalDateTime.now().plusHours(1);
        bookingDto.setStart(sameTime);
        bookingDto.setEnd(sameTime);
        bookingDto.setItemId(1L);

        assertThrows(ValidationException.class, () -> bookingService.create(bookingDto, userId));
    }

    @Test
    void approve_ShouldReturnRejectedBookingDto_WhenApprovedIsFalse() {
        Long bookingId = 1L;
        Long userId = 2L;
        Boolean approved = false;

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

        assertEquals(Status.REJECTED, result.getStatus());
        verify(bookingRepository).findById(bookingId);
        verify(itemRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void create_ShouldThrowValidationException_WhenStartEqualsEndPlusOneMinute() {
        Long userId = 1L;
        BookingCreateDto bookingDto = new BookingCreateDto();
        LocalDateTime now = LocalDateTime.now();
        bookingDto.setStart(now.plusMinutes(1));
        bookingDto.setEnd(now.plusMinutes(1));
        bookingDto.setItemId(1L);

        assertThrows(ValidationException.class, () -> bookingService.create(bookingDto, userId));
    }

    @Test
    void approve_ShouldThrowValidationException_WhenBookingStatusIsApproved() {
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
    }

    @Test
    void approve_ShouldThrowValidationException_WhenBookingStatusIsRejected() {
        Long bookingId = 1L;
        Long userId = 2L;
        Boolean approved = true;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setItemId(1L);
        booking.setBookerId(1L);
        booking.setStatus(Status.REJECTED);

        Item item = new Item();
        item.setId(1L);
        item.setOwnerId(userId);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(ValidationException.class, () -> bookingService.approve(bookingId, userId, approved));
        verify(bookingRepository).findById(bookingId);
        verify(itemRepository).findById(1L);
    }

    @Test
    void getUserBookings_ShouldUseDefaultState_WhenStateIsNull() {
        Long userId = 1L;
        String state = null;

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdOrderByIdDesc(userId)).thenReturn(List.of());

        List<BookingDto> result = bookingService.getUserBookings(userId, state);

        assertTrue(result.isEmpty());
        verify(userRepository).findById(userId);
        verify(bookingRepository).findByBookerIdOrderByIdDesc(userId);
    }

    @Test
    void getOwnerBookings_ShouldUseDefaultState_WhenStateIsNull() {
        Long userId = 1L;
        String state = null;

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookingRepository.findByItemOwnerIdOrderByIdDesc(userId)).thenReturn(List.of());

        List<BookingDto> result = bookingService.getOwnerBookings(userId, state);

        assertTrue(result.isEmpty());
        verify(userRepository).findById(userId);
        verify(bookingRepository).findByItemOwnerIdOrderByIdDesc(userId);
    }
}