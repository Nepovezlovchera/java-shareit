package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemBookDto;
import ru.practicum.shareit.user.dto.UserBookDto;

public class BookingMapper {

    public static BookingDto toBookingDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setStatus(booking.getStatus());

        if (booking.getItem() != null) {
            dto.setItem(new ItemBookDto(booking.getItem().getId(), booking.getItem().getName()));
        }

        if (booking.getBooker() != null) {
            dto.setBooker(new UserBookDto(booking.getBooker().getId()));
        }

        return dto;
    }

    public static Booking toBooking(BookingRequestDto requestDto) {
        if (requestDto == null) {
            return null;
        }

        Booking booking = new Booking();
        booking.setStart(requestDto.getStart());
        booking.setEnd(requestDto.getEnd());
        return booking;
    }
}