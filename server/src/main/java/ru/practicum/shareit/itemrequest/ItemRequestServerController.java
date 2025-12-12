package ru.practicum.shareit.itemrequest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.itemrequest.dto.ItemRequestDto;
import ru.practicum.shareit.itemrequest.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping("/server/requests")
@RequiredArgsConstructor
public class ItemRequestServerController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto create(@RequestBody ru.practicum.shareit.itemrequest.dto.ItemRequestCreateDto requestDto,
                                 @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.create(userId, requestDto.getDescription());
    }

    @GetMapping
    public List<ItemRequestDto> getOwnRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getOwnRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests(@RequestHeader("X-Sharer-User-Id") Long userId,
                                               @RequestParam(defaultValue = "0") Integer from,
                                               @RequestParam(defaultValue = "20") Integer size) {
        return itemRequestService.getAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequest(@PathVariable Long requestId,
                                     @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getRequestById(requestId, userId);
    }
}