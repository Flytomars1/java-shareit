package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingState;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> createBooking(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                                                @RequestBody BookingCreateDto bookingDto) {
        log.info("Получен POST-запрос на /bookings от пользователя id={}: start={}, end={}, itemId={}",
                userId, bookingDto.getStart(), bookingDto.getEnd(), bookingDto.getItemId());
        return bookingClient.bookItem(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                                                 @PathVariable Long bookingId,
                                                 @RequestParam Boolean approved) {
        log.info("Получен PATCH-запрос на /bookings/{} от пользователя id={}: approved={}",
                bookingId, userId, approved);
        return bookingClient.approveBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                                             @PathVariable Long bookingId) {
        log.info("Получен GET-запрос на /bookings/{} от пользователя id={}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserBookings(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                                                  @RequestParam(defaultValue = "ALL") BookingState state) {
        log.info("Получен GET-запрос на /bookings от пользователя id={} с параметром state={}", userId, state);
        return bookingClient.getBookings(userId, state, 0, 20);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerBookings(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                                                   @RequestParam(defaultValue = "ALL") BookingState state) {
        log.info("Получен GET-запрос на /bookings/owner от пользователя id={} с параметром state={}", userId, state);
        return bookingClient.getOwnerBookings(userId, state, 0, 20);
    }
}