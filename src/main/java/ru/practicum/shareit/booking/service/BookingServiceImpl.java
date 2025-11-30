package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingDto create(BookingCreateDto bookingDto, Long userId) {
        log.info("Создание бронирования пользователем id={}", userId);

        validateBookingDates(bookingDto.getStart(), bookingDto.getEnd());

        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found"));

        if (!item.getAvailable()) {
            throw new ValidationException("Item is not available for booking");
        }

        if (item.getOwnerId().equals(userId)) {
            throw new ValidationException("Cannot book own item");
        }

        Booking booking = BookingMapper.toBooking(bookingDto, userId);
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Бронирование id={} создано", savedBooking.getId());

        return BookingMapper.toBookingDto(savedBooking, item, booker);
    }

    @Override
    @Transactional
    public BookingDto approve(Long bookingId, Long userId, Boolean approved) {
        log.info("Подтверждение бронирования id={} пользователем id={}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        Item item = itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found"));

        if (!item.getOwnerId().equals(userId)) {
            throw new AccessDeniedException("Only owner can approve booking");
        }

        if (!booking.getStatus().equals(Status.WAITING)) {
            throw new ValidationException("Booking status is not WAITING");
        }

        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);

        User booker = userRepository.findById(booking.getBookerId())
                .orElseThrow(() -> new NotFoundException("Booker not found"));

        return BookingMapper.toBookingDto(updatedBooking, item, booker);
    }

    @Override
    public BookingDto getById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        Item item = itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found"));

        if (!booking.getBookerId().equals(userId) && !item.getOwnerId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        User booker = userRepository.findById(booking.getBookerId())
                .orElseThrow(() -> new NotFoundException("Booker not found"));

        return BookingMapper.toBookingDto(booking, item, booker);
    }

    @Override
    public List<BookingDto> getUserBookings(Long userId, BookingState state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByBookerIdOrderByIdDesc(userId);
            case CURRENT -> bookingRepository.findCurrentBookingsByBooker(userId, now);
            case PAST -> bookingRepository.findPastBookingsByBooker(userId, now);
            case FUTURE -> bookingRepository.findFutureBookingsByBooker(userId, now);
            case WAITING -> bookingRepository.findByBookerIdAndStatus(userId, Status.WAITING);
            case REJECTED -> bookingRepository.findByBookerIdAndStatus(userId, Status.REJECTED);
        };

        return bookings.stream()
                .map(this::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long userId, BookingState state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByItemOwnerIdOrderByIdDesc(userId);
            case CURRENT -> bookingRepository.findCurrentBookingsByOwner(userId, now);
            case PAST -> bookingRepository.findPastBookingsByOwner(userId, now);
            case FUTURE -> bookingRepository.findFutureBookingsByOwner(userId, now);
            case WAITING -> bookingRepository.findByItemOwnerIdAndStatus(userId, Status.WAITING);
            case REJECTED -> bookingRepository.findByItemOwnerIdAndStatus(userId, Status.REJECTED);
        };

        return bookings.stream()
                .map(this::toBookingDto)
                .collect(Collectors.toList());
    }

    private BookingDto toBookingDto(Booking booking) {
        Item item = itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found"));
        User booker = userRepository.findById(booking.getBookerId())
                .orElseThrow(() -> new NotFoundException("Booker not found"));
        return BookingMapper.toBookingDto(booking, item, booker);
    }

    private void validateBookingDates(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new ValidationException("Start and end dates must be specified");
        }
        if (!start.isBefore(end)) {
            throw new ValidationException("End date must be after start date");
        }
        if (!start.isAfter(LocalDateTime.now())) {
        }
    }
}