package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.comment.dto.CommentDto;

import java.util.List;

public interface ItemService {
    Item createItem(Long ownerId, Item item);

    Item updateItem(Long ownerId, Long itemId, Item item);

    Item getItemById(Long itemId);

    List<Item> getAllItemsByOwner(Long ownerId);

    List<Item> searchItems(String text);

    CommentDto addComment(Long authorId, Long itemId, String text);

    ItemWithBookingsDto getItemWithBookings(Long itemId, Long requesterId);

    List<ItemWithBookingsDto> getAllItemsByOwnerWithBookings(Long ownerId);
}