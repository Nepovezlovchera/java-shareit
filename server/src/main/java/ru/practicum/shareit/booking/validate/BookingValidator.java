package ru.practicum.shareit.booking.validate;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.status.Status;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;

public class BookingValidator {

    public static void validateForCreate(Item item, Booking booking, Long bookerId) {
        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }
        if (item.getOwner().getId().equals(bookerId)) {
            throw new NotFoundException("Владелец не может забронировать собственную вещь");
        }
        if (!booking.getEnd().isAfter(booking.getStart())) {
            throw new ValidationException("Дата окончания должна быть позже даты начала");
        }
    }

    public static void validateForApprove(Booking booking, Long ownerId) {
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Подтверждать бронирование может только владелец вещи");
        }
        if (booking.getStatus() != Status.WAITING) {
            throw new ValidationException("Бронирование уже обработано");
        }
    }

    public static void validateAccessToBooking(Booking booking, Long userId) {
        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new NotFoundException("Бронирование недоступно для этого пользователя");
        }
    }

}