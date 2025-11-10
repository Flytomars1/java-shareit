package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemRepository implements ItemRepository {

    private final Map<Long, Item> items = new ConcurrentHashMap<>();
    private final AtomicLong currentId = new AtomicLong(1);

    @Override
    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(currentId.getAndIncrement());
        }
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> findByOwnerId(Long ownerId) {
        return items.values().stream()
                .filter(item -> ownerId.equals(item.getOwnerId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        String query = text.toLowerCase().trim();
        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(query) ||
                        item.getDescription().toLowerCase().contains(query))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByIdAndOwnerId(Long itemId, Long ownerId) {
        Item item = items.get(itemId);
        return item != null && ownerId.equals(item.getOwnerId());
    }

    @Override
    public boolean existsById(Long id) {
        return items.containsKey(id);
    }
}