package ru.practicum.shareit.item.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public Item createItem(Long ownerId, Item item) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + ownerId + " не найден"));

        item.setOwner(owner);
        return itemRepository.save(item);
    }

    @Override
    public Item updateItem(Long ownerId, Long itemId, Item item) {
        Item existing = getItemById(itemId);

        if (!existing.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Редактировать вещь может только её владелец");
        }
        existing.setName(item.getName());
        existing.setDescription(item.getDescription());
        existing.setAvailable(item.getAvailable());

        return itemRepository.update(existing);
    }

    @Override
    public Item getItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));
    }

    @Override
    public List<Item> getAllItemsByOwner(Long ownerId) {
        return itemRepository.findAllByOwnerId(ownerId);
    }

    @Override
    public List<Item> searchItems(String text) {
        return itemRepository.search(text);
    }
}