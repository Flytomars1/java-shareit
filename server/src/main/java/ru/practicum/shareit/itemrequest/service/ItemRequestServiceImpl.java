package ru.practicum.shareit.itemrequest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.itemrequest.dto.ItemRequestDto;
import ru.practicum.shareit.itemrequest.dto.ItemRequestResponseDto;
import ru.practicum.shareit.itemrequest.model.ItemRequest;
import ru.practicum.shareit.itemrequest.repository.ItemRequestRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserService userService;

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, String description) {
        log.info("Создание запроса вещи пользователем id={}", userId);

        if (description == null || description.isBlank()) {
            throw new ValidationException("Description cannot be empty");
        }

        if (!userService.userExists(userId)) {
            log.warn("Попытка создания запроса для несуществующего пользователя id={}", userId);
            throw new NotFoundException("User not found");
        }

        ItemRequest request = new ItemRequest();
        request.setDescription(description.trim());
        request.setRequesterId(userId);
        request.setCreated(LocalDateTime.now());
        ItemRequest saved = itemRequestRepository.save(request);
        log.info("Запрос вещи создан с id={}", saved.getId());
        return toItemRequestDto(saved, List.of());
    }

    @Override
    public List<ItemRequestDto> getOwnRequests(Long userId) {
        log.info("Получение запросов вещей для пользователя id={}", userId);

        if (!userService.userExists(userId)) {
            log.warn("Попытка получения запросов для несуществующего пользователя id={}", userId);
            throw new NotFoundException("User not found");
        }

        List<ItemRequest> requests = itemRequestRepository.findByRequesterIdOrderByIdDesc(userId);
        return requests.stream()
                .map(this::toItemRequestDtoWithItems)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, Integer from, Integer size) {
        log.info("Получение всех запросов вещей (кроме своих) для пользователя id={}", userId);

        if (!userService.userExists(userId)) {
            log.warn("Попытка получения запросов для несуществующего пользователя id={}", userId);
            throw new NotFoundException("User not found");
        }

        List<ItemRequest> requests = itemRequestRepository.findByRequesterIdNotOrderByIdDesc(userId);
        return requests.stream()
                .map(this::toItemRequestDtoWithItems)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequestById(Long requestId, Long userId) {
        log.info("Получение запроса вещи id={} пользователем id={}", requestId, userId);

        if (!userService.userExists(userId)) {
            log.warn("Попытка получения запроса для несуществующего пользователя id={}", userId);
            throw new NotFoundException("User not found");
        }

        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.warn("Запрос вещи с id={} не найден", requestId);
                    return new NotFoundException("Item request not found");
                });
        return toItemRequestDtoWithItems(request);
    }

    private ItemRequestDto toItemRequestDtoWithItems(ItemRequest request) {
        List<Item> items = itemRepository.findByRequestIdOrderById(request.getId());
        List<ItemRequestResponseDto> itemDtos = items.stream()
                .map(item -> {
                    ItemRequestResponseDto dto = new ItemRequestResponseDto();
                    dto.setId(item.getId());
                    dto.setName(item.getName());
                    dto.setOwnerId(item.getOwnerId());
                    return dto;
                })
                .collect(Collectors.toList());
        return toItemRequestDto(request, itemDtos);
    }

    private ItemRequestDto toItemRequestDto(ItemRequest request, List<ItemRequestResponseDto> items) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setCreated(request.getCreated());
        dto.setItems(items);
        return dto;
    }
}