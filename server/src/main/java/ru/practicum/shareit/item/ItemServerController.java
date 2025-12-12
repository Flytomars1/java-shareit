package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/server/items")
@RequiredArgsConstructor
public class ItemServerController {

    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@RequestBody ItemDto itemDto,
                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.create(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@PathVariable Long itemId,
                          @RequestBody ItemDto itemDto,
                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.update(itemId, itemDto, userId);
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@PathVariable Long itemId,
                           @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.findById(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> getOwnerItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getOwnerItems(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        return itemService.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(
            @PathVariable Long itemId,
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestBody Map<String, String> request) {
        String text = request.get("text");
        return itemService.addComment(itemId, userId, text);
    }
}