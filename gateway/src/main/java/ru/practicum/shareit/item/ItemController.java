package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(value = USER_ID_HEADER, required = false) Long userId,
                                         @RequestBody ItemDto itemDto) {
        log.info("Получен POST-запрос на /items от пользователя id={}", userId);
        return itemClient.create(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@RequestHeader(value = USER_ID_HEADER, required = false) Long userId,
                                         @PathVariable Long itemId,
                                         @RequestBody ItemDto itemDto) {
        log.info("Получен PATCH-запрос на /items/{} от пользователя id={}", itemId, userId);
        return itemClient.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItem(@RequestHeader(value = USER_ID_HEADER, required = false) Long userId,
                                          @PathVariable Long itemId) {
        log.info("Получен GET-запрос на /items/{} от пользователя id={}", itemId, userId);
        return itemClient.getItem(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getItems(@RequestHeader(value = USER_ID_HEADER, required = false) Long userId) {
        log.info("Получен GET-запрос на /items от владельца id={}", userId);
        return itemClient.getItems(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam String text) {
        log.info("Получен GET-запрос на /items/search с text='{}'", text);
        return itemClient.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(value = USER_ID_HEADER, required = false) Long userId,
                                             @PathVariable Long itemId,
                                             @RequestBody Map<String, String> request) {
        String text = request.get("text");
        log.info("Получен POST-запрос на /items/{}/comment от пользователя id={}: text='{}'", itemId, userId, text);
        return itemClient.addComment(userId, itemId, text);
    }
}