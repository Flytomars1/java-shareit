package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
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
                .orElseThrow(() -> {
                    log.warn("Попытка создания бронирования несуществующим пользователем id={}", userId);
                    return new NotFoundException("User not found");
                });

        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> {
                    log.warn("Попытка бронирования несуществующей вещи id={}", bookingDto.getItemId());
                    return new NotFoundException("Item not found");
                });

        if (!item.getAvailable()) {
            log.warn("Попытка бронирования недоступной вещи id={}", bookingDto.getItemId());
            throw new ValidationException("Item is not available for booking");
        }

        if (item.getOwnerId().equals(userId)) {
            log.warn("Попытка бронирования своей вещи пользователем id={}", userId);
            throw new ValidationException("Cannot book own item");
        }

        Booking booking = BookingMapper.toBooking(bookingDto, userId);
        booking.setStatus(Status.WAITING);
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Бронирование id={} создано", savedBooking.getId());

        return BookingMapper.toBookingDto(savedBooking, item, booker);
    }

    @Override
    @Transactional
    public BookingDto approve(Long bookingId, Long userId, Boolean approved) {
        log.info("Подтверждение бронирования id={} пользователем id={}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("Попытка подтверждения несуществующего бронирования id={}", bookingId);
                    return new NotFoundException("Booking not found");
                });

        Item item = itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> {
                    log.warn("Вещь бронирования id={} не найдена", bookingId);
                    return new NotFoundException("Item not found");
                });

        if (!item.getOwnerId().equals(userId)) {
            log.warn("Попытка подтверждения бронирования не владельцем вещи id={}", bookingId);
            throw new AccessDeniedException("Only owner can approve booking");
        }

        if (!booking.getStatus().equals(Status.WAITING)) {
            log.warn("Попытка подтверждения бронирования с уже установленным статусом: {}", booking.getStatus());
            throw new ValidationException("Booking status is not WAITING");
        }

        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Статус бронирования id={} изменен на {}", bookingId, booking.getStatus());

        User booker = userRepository.findById(booking.getBookerId())
                .orElseThrow(() -> new NotFoundException("Booker not found"));

        return BookingMapper.toBookingDto(updatedBooking, item, booker);
    }

    @Override
    public BookingDto getById(Long bookingId, Long userId) {
        log.info("Получение бронирования id={} пользователем id={}", bookingId, userId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("Попытка получения несуществующего бронирования id={}", bookingId);
                    return new NotFoundException("Booking not found");
                });

        Item item = itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found"));

        if (!booking.getBookerId().equals(userId) && !item.getOwnerId().equals(userId)) {
            log.warn("Попытка получения чужого бронирования id={} пользователем id={}", bookingId, userId);
            throw new AccessDeniedException("Access denied");
        }

        User booker = userRepository.findById(booking.getBookerId())
                .orElseThrow(() -> new NotFoundException("Booker not found"));

        return BookingMapper.toBookingDto(booking, item, booker);
    }

    @Override
    public List<BookingDto> getUserBookings(Long userId, String state) {
        log.info("Получение бронирований пользователя id={} с состоянием {}", userId, state);
        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Попытка получения бронирований несуществующим пользователем id={}", userId);
                    return new NotFoundException("User not found");
                });

        if (state == null || state.isBlank()) {
            state = "ALL";
        }

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state.toUpperCase()) {
            case "ALL":
                bookings = bookingRepository.findByBookerIdOrderByIdDesc(userId);
                break;
            case "CURRENT":
                bookings = bookingRepository.findCurrentBookingsByBooker(userId, now);
                break;
            case "PAST":
                bookings = bookingRepository.findPastBookingsByBooker(userId, now);
                break;
            case "FUTURE":
                bookings = bookingRepository.findFutureBookingsByBooker(userId, now);
                break;
            case "WAITING":
                bookings = bookingRepository.findByBookerIdAndStatus(userId, Status.WAITING);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByBookerIdAndStatus(userId, Status.REJECTED);
                break;
            default:
                log.warn("Некорректное состояние бронирования: {}", state);
                throw new ValidationException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(this::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long userId, String state) {
        log.info("Получение бронирований вещей владельца id={} с состоянием {}", userId, state);
        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Попытка получения бронирований вещей несуществующим владельцем id={}", userId);
                    return new NotFoundException("User not found");
                });

        if (state == null || state.isBlank()) {
            state = "ALL";
        }

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state.toUpperCase()) {
            case "ALL":
                bookings = bookingRepository.findByItemOwnerIdOrderByIdDesc(userId);
                break;
            case "CURRENT":
                bookings = bookingRepository.findCurrentBookingsByOwner(userId, now);
                break;
            case "PAST":
                bookings = bookingRepository.findPastBookingsByOwner(userId, now);
                break;
            case "FUTURE":
                bookings = bookingRepository.findFutureBookingsByOwner(userId, now);
                break;
            case "WAITING":
                bookings = bookingRepository.findByItemOwnerIdAndStatus(userId, Status.WAITING);
                break;
            case "REJECTED":
                bookings = bookingRepository.findByItemOwnerIdAndStatus(userId, Status.REJECTED);
                break;
            default:
                log.warn("Некорректное состояние бронирования: {}", state);
                throw new ValidationException("Unknown state: " + state);
        }

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
        if (start.isBefore(LocalDateTime.now().minusMinutes(1))) {
            throw new ValidationException("Start date must be in the future");
        }
    }
}