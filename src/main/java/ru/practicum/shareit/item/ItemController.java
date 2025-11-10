package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {

    private static final Logger log = LoggerFactory.getLogger(ItemController.class);
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto create(@RequestHeader(USER_ID_HEADER) Long userId,
                          @RequestBody ItemDto itemDto) {
        log.info("Получен POST-запрос на /items от пользователя с id={}: name='{}', description='{}', available={}",
                userId, itemDto.getName(), itemDto.getDescription(), itemDto.getAvailable());
        return itemService.create(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader(USER_ID_HEADER) Long userId,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) {
        log.info("Получен PATCH-запрос на /items/{} от пользователя с id={}: name='{}', description='{}', available={}",
                itemId, userId,
                itemDto.getName() != null ? itemDto.getName() : "UNCHANGED",
                itemDto.getDescription() != null ? itemDto.getDescription() : "UNCHANGED",
                itemDto.getAvailable() != null ? itemDto.getAvailable() : "UNCHANGED");
        return itemService.update(itemId, itemDto, userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@PathVariable Long itemId,
                           @RequestHeader(value = USER_ID_HEADER, required = false) Long userId) {
        log.info("Получен GET-запрос на /items/{} от пользователя с id={}", itemId, userId);
        return itemService.findById(itemId, userId != null ? userId : -1L);
    }

    @GetMapping
    public List<ItemDto> getOwnerItems(@RequestHeader(USER_ID_HEADER) Long userId) {
        log.info("Получен GET-запрос на /items от владельца с id={}", userId);
        return itemService.findByOwnerId(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text,
                                     @RequestHeader(value = USER_ID_HEADER, required = false) Long userId) {
        log.info("Получен GET-запрос на /items/search с text='{}' от пользователя с id={}", text, userId);
        return itemService.search(text);
    }
}