package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemRepository implements ItemRepository {

    private final Map<Long, Item> items = new HashMap<>();
    private final Map<Long, List<Item>> itemsByOwner = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    @Override
    public List<Item> findAllByOwnerId(Long ownerId) {
        return new ArrayList<>(itemsByOwner.getOrDefault(ownerId, List.of()));
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public Item save(Item item) {
        item.setId(idCounter.incrementAndGet());
        items.put(item.getId(), item);
        itemsByOwner.computeIfAbsent(item.getOwner().getId(), k -> new ArrayList<>()).add(item);
        return item;
    }

    @Override
    public Item update(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public List<Item> search(String text) {
        String lower = text.toLowerCase();
        return items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> (item.getName() != null && item.getName().toLowerCase().contains(lower))
                        || (item.getDescription() != null && item.getDescription().toLowerCase().contains(lower)))
                .collect(Collectors.toList());
    }
}