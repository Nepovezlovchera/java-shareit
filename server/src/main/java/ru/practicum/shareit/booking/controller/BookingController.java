package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final BookingService bookingService;

    @PostMapping
    public BookingDto create(@RequestHeader(USER_ID_HEADER) Long bookerId,
                             @RequestBody BookingRequestDto requestDto) {
        Booking booking = BookingMapper.toBooking(requestDto);
        Booking created = bookingService.create(bookerId, requestDto.getItemId(), booking);
        return BookingMapper.toBookingDto(created);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approve(@RequestHeader(USER_ID_HEADER) Long ownerId,
                              @PathVariable Long bookingId,
                              @RequestParam boolean approved) {
        Booking booking = bookingService.approve(ownerId, bookingId, approved);
        return BookingMapper.toBookingDto(booking);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getById(@RequestHeader(USER_ID_HEADER) Long userId,
                              @PathVariable Long bookingId) {
        Booking booking = bookingService.getById(userId, bookingId);
        return BookingMapper.toBookingDto(booking);
    }

    @GetMapping
    public List<BookingDto> getAllByBooker(@RequestHeader(USER_ID_HEADER) Long bookerId,
                                           @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getAllByBooker(bookerId, state).stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllByOwner(@RequestHeader(USER_ID_HEADER) Long ownerId,
                                          @RequestParam(defaultValue = "ALL") String state) {
        return bookingService.getAllByOwner(ownerId, state).stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }
}