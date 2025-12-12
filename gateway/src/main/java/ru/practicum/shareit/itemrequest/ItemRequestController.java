package ru.practicum.shareit.itemrequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.itemrequest.dto.ItemRequestCreateDto;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestHeader(value = USER_ID_HEADER, required = false) Long userId,
                                                @RequestBody ItemRequestCreateDto requestDto) {
        log.info("Создание запроса вещи пользователем id={}", userId);
        return itemRequestClient.create(userId, requestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnRequests(@RequestHeader(value = USER_ID_HEADER, required = false) Long userId) {
        log.info("Получение своих запросов пользователем id={}", userId);
        return itemRequestClient.getOwnRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader(value = USER_ID_HEADER, required = false) Long userId,
                                                 @RequestParam(defaultValue = "0") Integer from,
                                                 @RequestParam(defaultValue = "20") Integer size) {
        log.info("Получение всех запросов (кроме своих) пользователем id={}", userId);
        return itemRequestClient.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequest(@RequestHeader(value = USER_ID_HEADER, required = false) Long userId,
                                             @PathVariable Long requestId) {
        log.info("Получение запроса id={} пользователем id={}", requestId, userId);
        return itemRequestClient.getRequest(userId, requestId);
    }
}