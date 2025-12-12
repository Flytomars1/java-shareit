package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BookingServiceIntegrationTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private BookingServiceImpl bookingService;

    private User booker;
    private User owner;
    private Item item;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(bookingRepository, userRepository, itemRepository);

        // Создаем тестовых пользователей
        booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@example.com");
        booker = userRepository.save(booker);

        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@example.com");
        owner = userRepository.save(owner);

        // Создаем тестовую вещь
        item = new Item();
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwnerId(owner.getId());
        item = itemRepository.save(item);
    }

    @Test
    void create_ShouldSaveBooking_WhenValidInput() {
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setStart(LocalDateTime.now().plusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(2));
        bookingDto.setItemId(item.getId());

        BookingDto result = bookingService.create(bookingDto, booker.getId());

        assertNotNull(result.getId());
        assertEquals(Status.WAITING, result.getStatus());

        // Проверяем, что бронирование действительно сохранилось в базе
        assertEquals(1, bookingRepository.findByBookerIdOrderByIdDesc(booker.getId()).size());
    }

    @Test
    void create_ShouldThrowNotFoundException_WhenItemDoesNotExist() {
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setStart(LocalDateTime.now().plusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(2));
        bookingDto.setItemId(999L);

        assertThrows(NotFoundException.class, () -> bookingService.create(bookingDto, booker.getId()));
    }

    @Test
    void getUserBookings_ShouldReturnBookings_WhenUserExists() {
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setStart(LocalDateTime.now().plusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(2));
        bookingDto.setItemId(item.getId());

        BookingDto createdBooking = bookingService.create(bookingDto, booker.getId());

        List<BookingDto> result = bookingService.getUserBookings(booker.getId(), "ALL");

        assertEquals(1, result.size());
        assertEquals(createdBooking.getId(), result.get(0).getId());
    }

    @Test
    void approve_ShouldUpdateBookingStatus_WhenValidInput() {
        BookingCreateDto bookingDto = new BookingCreateDto();
        bookingDto.setStart(LocalDateTime.now().plusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(2));
        bookingDto.setItemId(item.getId());

        BookingDto createdBooking = bookingService.create(bookingDto, booker.getId());

        BookingDto approvedBooking = bookingService.approve(createdBooking.getId(), owner.getId(), true);

        assertEquals(Status.APPROVED, approvedBooking.getStatus());

        // Проверяем, что бронирование в базе обновлено
        var updatedBooking = bookingRepository.findById(createdBooking.getId()).orElse(null);
        assertNotNull(updatedBooking);
        assertEquals(Status.APPROVED, updatedBooking.getStatus());
    }
}