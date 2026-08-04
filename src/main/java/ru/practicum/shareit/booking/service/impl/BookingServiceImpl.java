package ru.practicum.shareit.booking.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.status.BookingState;
import ru.practicum.shareit.booking.status.Status;
import ru.practicum.shareit.booking.validate.BookingValidator;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingValidator validator;

    @Override
    public Booking create(Long bookerId, Long itemId, Booking booking) {
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + bookerId + " не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        validator.validateForCreate(item, booking, bookerId);

        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Status.WAITING);

        return bookingRepository.save(booking);
    }

    @Override
    public Booking approve(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = getBookingOrThrow(bookingId);

        validator.validateForApprove(booking, ownerId);

        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        return bookingRepository.save(booking);
    }

    @Override
    public Booking getById(Long userId, Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);

        validator.validateAccessToBooking(booking, userId);

        return booking;
    }

    @Override
    public List<Booking> getAllByBooker(Long bookerId, String stateParam) {
        userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + bookerId + " не найден"));

        BookingState state = validator.parseState(stateParam);
        LocalDateTime now = LocalDateTime.now();
        return switch (state) {
            case CURRENT -> bookingRepository.findByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(bookerId, now, now);
            case PAST -> bookingRepository.findByBooker_IdAndEndBeforeOrderByStartDesc(bookerId, now);
            case FUTURE -> bookingRepository.findByBooker_IdAndStartAfterOrderByStartDesc(bookerId, now);
            case WAITING -> bookingRepository.findByBooker_IdAndStatusOrderByStartDesc(bookerId, Status.WAITING);
            case REJECTED -> bookingRepository.findByBooker_IdAndStatusOrderByStartDesc(bookerId, Status.REJECTED);
            case ALL -> bookingRepository.findByBooker_IdOrderByStartDesc(bookerId);
        };
    }

    @Override
    public List<Booking> getAllByOwner(Long ownerId, String stateParam) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + ownerId + " не найден"));

        BookingState state = validator.parseState(stateParam);
        LocalDateTime now = LocalDateTime.now();
        return switch (state) {
            case CURRENT -> bookingRepository.findByItem_Owner_IdAndStartBeforeAndEndAfterOrderByStartDesc(ownerId, now, now);
            case PAST -> bookingRepository.findByItem_Owner_IdAndEndBeforeOrderByStartDesc(ownerId, now);
            case FUTURE -> bookingRepository.findByItem_Owner_IdAndStartAfterOrderByStartDesc(ownerId, now);
            case WAITING -> bookingRepository.findByItem_Owner_IdAndStatusOrderByStartDesc(ownerId, Status.WAITING);
            case REJECTED -> bookingRepository.findByItem_Owner_IdAndStatusOrderByStartDesc(ownerId, Status.REJECTED);
            case ALL -> bookingRepository.findByItem_Owner_IdOrderByStartDesc(ownerId);
        };
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id=" + bookingId + " не найдено"));
    }


}
