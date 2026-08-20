package ru.practicum.shareit.book.validator;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.status.Status;
import ru.practicum.shareit.booking.validate.BookingValidator;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingValidatorTest {

    @Test
    void validateForCreate_withValidData_shouldNotThrow() {
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setOwner(owner);
        item.setAvailable(true);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));

        assertThatCode(() -> BookingValidator.validateForCreate(item, booking, 2L))
                .doesNotThrowAnyException();
    }

    @Test
    void validateForCreate_withUnavailableItem_shouldThrowValidationException() {
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setOwner(owner);
        item.setAvailable(false);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> BookingValidator.validateForCreate(item, booking, 2L))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validateForCreate_byOwnerHimself_shouldThrowNotFoundException() {
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setOwner(owner);
        item.setAvailable(true);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> BookingValidator.validateForCreate(item, booking, 1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void validateForCreate_withEndBeforeStart_shouldThrowValidationException() {
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setOwner(owner);
        item.setAvailable(true);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(2));
        booking.setEnd(LocalDateTime.now().plusDays(1));

        assertThatThrownBy(() -> BookingValidator.validateForCreate(item, booking, 2L))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validateForApprove_byOwner_shouldNotThrow() {
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setStatus(Status.WAITING);

        assertThatCode(() -> BookingValidator.validateForApprove(booking, 1L))
                .doesNotThrowAnyException();
    }

    @Test
    void validateForApprove_byNotOwner_shouldThrowForbiddenException() {
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setStatus(Status.WAITING);

        assertThatThrownBy(() -> BookingValidator.validateForApprove(booking, 2L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void validateForApprove_alreadyProcessed_shouldThrowValidationException() {
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setStatus(Status.APPROVED);

        assertThatThrownBy(() -> BookingValidator.validateForApprove(booking, 1L))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void validateAccessToBooking_byBooker_shouldNotThrow() {
        User booker = new User();
        booker.setId(2L);

        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setBooker(booker);
        booking.setItem(item);

        assertThatCode(() -> BookingValidator.validateAccessToBooking(booking, 2L))
                .doesNotThrowAnyException();
    }

    @Test
    void validateAccessToBooking_byOwner_shouldNotThrow() {
        User booker = new User();
        booker.setId(2L);

        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setBooker(booker);
        booking.setItem(item);

        assertThatCode(() -> BookingValidator.validateAccessToBooking(booking, 1L))
                .doesNotThrowAnyException();
    }

    @Test
    void validateAccessToBooking_byStranger_shouldThrowNotFoundException() {
        User booker = new User();
        booker.setId(2L);

        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setOwner(owner);

        Booking booking = new Booking();
        booking.setBooker(booker);
        booking.setItem(item);

        assertThatThrownBy(() -> BookingValidator.validateAccessToBooking(booking, 3L))
                .isInstanceOf(NotFoundException.class);
    }
}
