package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto create(ItemDto itemDto, Long userId);
    ItemDto update(Long itemId, ItemDto itemDto, Long userId);
    ItemDto findById(Long itemId, Long userId);
    List<ItemDto> findByOwnerId(Long userId);
    List<ItemDto> search(String text);
}