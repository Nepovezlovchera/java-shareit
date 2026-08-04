package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.status.BookingState;

import java.util.List;

public interface BookingService {

    Booking create(Long bookerId, Long itemId, Booking booking);

    Booking approve(Long ownerId, Long bookingId, boolean approved);

    Booking getById(Long userId, Long bookingId);

    List<Booking> getAllByBooker(Long bookerId, String state);

    List<Booking> getAllByOwner(Long ownerId, String state);
}
