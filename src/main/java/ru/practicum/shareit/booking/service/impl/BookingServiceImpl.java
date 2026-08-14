package ru.practicum.shareit.booking.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.BookingFetcher;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.status.BookingState;
import ru.practicum.shareit.booking.status.Status;
import ru.practicum.shareit.booking.validate.BookingValidator;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
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

    @Override
    public Booking create(Long bookerId, Long itemId, Booking booking) {
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + bookerId + " не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        BookingValidator.validateForCreate(item, booking, bookerId);

        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                itemId, booking.getStart(), booking.getEnd());
        if (!conflicts.isEmpty()) {
            throw new ValidationException("Вещь уже забронирована на выбранные даты");
        }

        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Status.WAITING);

        return bookingRepository.save(booking);
    }

    @Override
    public Booking approve(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = getBookingOrThrow(bookingId);

        BookingValidator.validateForApprove(booking, ownerId);

        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        return bookingRepository.save(booking);
    }

    @Override
    public Booking getById(Long userId, Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);

        BookingValidator.validateAccessToBooking(booking, userId);

        return booking;
    }

    @Override
    public List<Booking> getAllByBooker(Long bookerId, String stateParam) {
        userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + bookerId + " не найден"));

        BookingState state = BookingState.parseState(stateParam);

        return BookingFetcher.getBookingsByState(state, new BookingFetcher() {
            @Override
            public List<Booking> all() {
                return bookingRepository.findByBooker_IdOrderByStartDesc(bookerId);
            }

            @Override
            public List<Booking> current(LocalDateTime now) {
                return bookingRepository.findByBooker_IdAndStartBeforeAndEndAfterOrderByStartDesc(bookerId, now, now);
            }

            @Override
            public List<Booking> past(LocalDateTime now) {
                return bookingRepository.findByBooker_IdAndEndBeforeOrderByStartDesc(bookerId, now);
            }

            @Override
            public List<Booking> future(LocalDateTime now) {
                return bookingRepository.findByBooker_IdAndStartAfterOrderByStartDesc(bookerId, now);
            }

            @Override
            public List<Booking> byStatus(Status status) {
                return bookingRepository.findByBooker_IdAndStatusOrderByStartDesc(bookerId, status);
            }
        });
    }

    @Override
    public List<Booking> getAllByOwner(Long ownerId, String stateParam) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + ownerId + " не найден"));

        BookingState state = BookingState.parseState(stateParam);

        return BookingFetcher.getBookingsByState(state, new BookingFetcher() {
            @Override
            public List<Booking> all() {
                return bookingRepository.findByItem_Owner_IdOrderByStartDesc(ownerId);
            }

            @Override
            public List<Booking> current(LocalDateTime now) {
                return bookingRepository.findByItem_Owner_IdAndStartBeforeAndEndAfterOrderByStartDesc(ownerId, now, now);
            }

            @Override
            public List<Booking> past(LocalDateTime now) {
                return bookingRepository.findByItem_Owner_IdAndEndBeforeOrderByStartDesc(ownerId, now);
            }

            @Override
            public List<Booking> future(LocalDateTime now) {
                return bookingRepository.findByItem_Owner_IdAndStartAfterOrderByStartDesc(ownerId, now);
            }

            @Override
            public List<Booking> byStatus(Status status) {
                return bookingRepository.findByItem_Owner_IdAndStatusOrderByStartDesc(ownerId, status);
            }
        });
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id=" + bookingId + " не найдено"));
    }

}
