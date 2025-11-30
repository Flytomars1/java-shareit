package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ItemDto create(ItemDto itemDto, Long userId) {
        log.info("Создание вещи для пользователя id={}", userId);
        validateItem(itemDto);

        if (!userService.userExists(userId)) {
            log.warn("Попытка создания вещи для несуществующего пользователя id={}", userId);
            throw new NotFoundException("User not found");
        }

        Item item = ItemMapper.toItem(itemDto, userId);
        Item savedItem = itemRepository.save(item);
        log.info("Вещь создана с id={} для пользователя id={}", savedItem.getId(), userId);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto update(Long itemId, ItemDto itemDto, Long userId) {
        log.info("Обновление вещи id={} пользователем id={}", itemId, userId);
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.warn("Вещь с id={} не найдена", itemId);
                    return new NotFoundException("Item not found");
                });

        if (!existingItem.getOwnerId().equals(userId)) {
            log.warn("Пользователь id={} не является владельцем вещи id={}", userId, itemId);
            throw new NotFoundException("Item not found");
        }

        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(existingItem);
        log.info("Вещь id={} обновлена", itemId);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto findById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));
        return toItemDtoWithBookings(item, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> search(String text) {
        log.info("Поиск вещей по тексту: '{}'", text);
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.searchByText(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getOwnerItems(Long userId) {
        log.info("Получение списка вещей владельца с id={}", userId);
        List<Item> items = itemRepository.findByOwnerIdOrderById(userId);
        return items.stream()
                .map(item -> toItemDtoWithBookings(item, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long itemId, Long userId, String text) {
        log.info("Добавление комментария к вещи id={} пользователем id={}", itemId, userId);
        LocalDateTime now = LocalDateTime.now();

        if (text == null || text.isBlank()) {
            throw new ValidationException("Comment text cannot be empty");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        if (!hasUserBookedAndFinishedItem(userId, itemId, now)) {
            throw new ValidationException("User has not booked this item or booking has not finished yet");
        }

        Comment comment = new Comment();
        comment.setText(text);
        comment.setItemId(itemId);
        comment.setAuthorId(userId);
        comment.setCreated(now);

        Comment savedComment = commentRepository.save(comment);

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Author not found"));

        CommentDto dto = new CommentDto();
        dto.setId(savedComment.getId());
        dto.setText(savedComment.getText());
        dto.setAuthorName(author.getName());
        dto.setCreated(savedComment.getCreated());

        return dto;
    }

    private ItemDto toItemDtoWithBookings(Item item, Long requesterId) {
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());

        if (requesterId != null && requesterId.equals(item.getOwnerId())) {
            LocalDateTime now = LocalDateTime.now();

            Booking last = bookingRepository.findLastBookingsByItemIdAndStatus(item.getId(), Status.APPROVED, now)
                    .stream().findFirst().orElse(null);
            Booking next = bookingRepository.findNextBookingsByItemIdAndStatus(item.getId(), Status.APPROVED, now)
                    .stream().findFirst().orElse(null);

            dto.setLastBooking(last != null ? toBookingShortDto(last) : null);
            dto.setNextBooking(next != null ? toBookingShortDto(next) : null);
        } else {
            dto.setLastBooking(null);
            dto.setNextBooking(null);
        }

        dto.setComments(loadCommentsForItem(item.getId()));

        return dto;
    }

    private BookingShortDto toBookingShortDto(Booking booking) {
        BookingShortDto dto = new BookingShortDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setBookerId(booking.getBookerId());
        return dto;
    }

    private void validateItem(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new RuntimeException("Item name must not be blank");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new RuntimeException("Item description must not be blank");
        }
        if (itemDto.getAvailable() == null) {
            throw new RuntimeException("Item availability must be specified");
        }
    }

    private List<CommentDto> loadCommentsForItem(Long itemId) {
        return commentRepository.findByItemIdOrderById(itemId).stream()
                .map(comment -> {
                    User author = userRepository.findById(comment.getAuthorId())
                            .orElseThrow(() -> new RuntimeException("Author not found"));
                    CommentDto dto = new CommentDto();
                    dto.setId(comment.getId());
                    dto.setText(comment.getText());
                    dto.setAuthorName(author.getName());
                    dto.setCreated(comment.getCreated());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private boolean hasUserBookedAndFinishedItem(Long userId, Long itemId, LocalDateTime now) {
        return bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(
                userId, itemId, Status.APPROVED, now
        );
    }
}