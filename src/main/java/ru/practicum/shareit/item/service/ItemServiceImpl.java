package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    private static final Logger log = LoggerFactory.getLogger(ItemServiceImpl.class);

    private final ItemRepository itemRepository;
    private final UserService userService;

    public ItemServiceImpl(ItemRepository itemRepository, UserService userService) {
        this.itemRepository = itemRepository;
        this.userService = userService;
    }

    @Override
    public ItemDto create(ItemDto itemDto, Long userId) {
        log.info("Начало создания вещи для пользователя с id={}", userId);
        validateItem(itemDto);

        if (!userService.userExists(userId)) {
            log.warn("Попытка создания вещи для несуществующего пользователя с id={}", userId);
            throw new ItemNotFoundException("User not found");
        }

        Item item = ItemMapper.toItem(itemDto, userId);
        Item savedItem = itemRepository.save(item);
        log.info("Вещь создана с id={} для пользователя с id={}", savedItem.getId(), userId);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto update(Long itemId, ItemDto itemDto, Long userId) {
        log.info("Начало обновления вещи с id={} пользователем с id={}", itemId, userId);

        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Попытка обновить несуществующую вещь с id={}", itemId);
                    return new ItemNotFoundException("Item not found");
                });

        if (!existingItem.getOwnerId().equals(userId)) {
            log.warn("Пользователь с id={} попытался обновить чужую вещь с id={}", userId, itemId);
            throw new ItemNotFoundException("Item not found");
        }

        boolean changed = false;
        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
            changed = true;
            log.debug("Название вещи id={} обновлено на '{}'", itemId, itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
            changed = true;
            log.debug("Описание вещи id={} обновлено на '{}'", itemId, itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
            changed = true;
            log.debug("Статус доступности вещи id={} установлен в {}", itemId, itemDto.getAvailable());
        }

        if (!changed) {
            log.debug("Вещь с id={} не была изменена — все поля null", itemId);
        }

        Item updatedItem = itemRepository.save(existingItem);
        log.info("Вещь с id={} успешно обновлена пользователем с id={}", itemId, userId);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto findById(Long itemId, Long userId) {
        log.info("Запрос вещи с id={}", itemId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Попытка получить несуществующую вещь с id={}", itemId);
                    return new ItemNotFoundException("Item not found");
                });
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> findByOwnerId(Long userId) {
        log.info("Запрос списка вещей владельца с id={}", userId);
        List<ItemDto> items = itemRepository.findByOwnerId(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        log.debug("Найдено {} вещей у владельца с id={}", items.size(), userId);
        return items;
    }

    @Override
    public List<ItemDto> search(String text) {
        log.info("Поиск вещей по тексту: '{}'", text);
        if (text == null || text.isBlank()) {
            log.debug("Пустой или null-запрос поиска — возвращён пустой список");
            return List.of();
        }
        List<ItemDto> results = itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
        log.debug("По запросу '{}' найдено {} вещей", text, results.size());
        return results;
    }

    private void validateItem(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            log.warn("Попытка создания вещи с пустым названием");
            throw new RuntimeException("Item name must not be blank");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            log.warn("Попытка создания вещи с пустым описанием");
            throw new RuntimeException("Item description must not be blank");
        }
        if (itemDto.getAvailable() == null) {
            log.warn("Попытка создания вещи без указания доступности");
            throw new RuntimeException("Item availability must be specified");
        }
    }
}