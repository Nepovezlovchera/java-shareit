package ru.practicum.shareit.item.validate;

import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.model.Item;

public class ItemValidator {

    private ItemValidator() {
    }

    public static void validateOwner(Item item, Long ownerId) {
        if (!item.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Редактировать вещь может только её владелец");
        }
    }
}