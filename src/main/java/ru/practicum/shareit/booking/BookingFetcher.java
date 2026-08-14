package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.status.BookingState;
import ru.practicum.shareit.booking.status.Status;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingFetcher {
    List<Booking> all();
    List<Booking> current(LocalDateTime now);
    List<Booking> past(LocalDateTime now);
    List<Booking> future(LocalDateTime now);
    List<Booking> byStatus(Status status);

    static List<Booking> getBookingsByState(BookingState state, BookingFetcher fetcher) {
        LocalDateTime now = LocalDateTime.now();
        return switch (state) {
            case CURRENT -> fetcher.current(now);
            case PAST -> fetcher.past(now);
            case FUTURE -> fetcher.future(now);
            case WAITING -> fetcher.byStatus(Status.WAITING);
            case REJECTED -> fetcher.byStatus(Status.REJECTED);
            case ALL -> fetcher.all();
        };
    }
}
