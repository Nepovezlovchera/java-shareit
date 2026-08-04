package ru.practicum.shareit.item.validate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.status.Status;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ItemValidator {

    private final BookingRepository bookingRepository;

    public void validateOwner(Item item, Long ownerId) {
        if (!item.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Редактировать вещь может только её владелец");
        }
    }

    public void validateHasCompletedBooking(Long authorId, Long itemId) {
        boolean hasCompletedBooking = bookingRepository
                .findByBooker_IdAndItem_IdAndEndBeforeAndStatus(
                        authorId, itemId, LocalDateTime.now(), Status.APPROVED)
                .stream()
                .findAny()
                .isPresent();

        if (!hasCompletedBooking) {
            throw new ValidationException("Оставить отзыв может только пользователь, завершивший аренду этой вещи");
        }
    }
}