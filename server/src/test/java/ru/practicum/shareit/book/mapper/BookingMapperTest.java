package ru.practicum.shareit.book.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.status.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BookingMapperTest {

    @Test
    void toBookingDto_shouldMapAllFields() {
        User booker = new User();
        booker.setId(2L);

        Item item = new Item();
        item.setId(3L);
        item.setName("Дрель");

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setStatus(Status.WAITING);
        booking.setItem(item);
        booking.setBooker(booker);

        BookingDto dto = BookingMapper.toBookingDto(booking);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(start);
        assertThat(dto.getEnd()).isEqualTo(end);
        assertThat(dto.getStatus()).isEqualTo(Status.WAITING);
        assertThat(dto.getItem().getId()).isEqualTo(3L);
        assertThat(dto.getItem().getName()).isEqualTo("Дрель");
        assertThat(dto.getBooker().getId()).isEqualTo(2L);
    }

    @Test
    void toBooking_shouldMapStartAndEndOnly() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setStart(start);
        requestDto.setEnd(end);

        Booking booking = BookingMapper.toBooking(requestDto);

        assertThat(booking.getStart()).isEqualTo(start);
        assertThat(booking.getEnd()).isEqualTo(end);
        assertThat(booking.getId()).isNull();
        assertThat(booking.getItem()).isNull();
        assertThat(booking.getBooker()).isNull();
    }
}
