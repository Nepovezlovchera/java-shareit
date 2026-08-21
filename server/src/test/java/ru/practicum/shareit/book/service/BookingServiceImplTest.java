package ru.practicum.shareit.book.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.status.Status;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class BookingServiceImplTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private EntityManager em;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = persistUser("owner@example.com");
        booker = persistUser("booker@example.com");
        item = persistItem(owner, "Дрель", true);
    }

    @Test
    void create_shouldPersistBookingWithWaitingStatus() {
        Booking booking = newBooking(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        Booking created = bookingService.create(booker.getId(), item.getId(), booking);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getStatus()).isEqualTo(Status.WAITING);
        assertThat(created.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(created.getItem().getId()).isEqualTo(item.getId());
    }

    @Test
    void create_withUnavailableItem_shouldThrowValidationException() {
        Item unavailable = persistItem(owner, "Молоток", false);
        Booking booking = newBooking(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.create(booker.getId(), unavailable.getId(), booking))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_byOwnerHimself_shouldThrowNotFoundException() {
        Booking booking = newBooking(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.create(owner.getId(), item.getId(), booking))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_withEndBeforeStart_shouldThrowValidationException() {
        Booking booking = newBooking(LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(1));

        assertThatThrownBy(() -> bookingService.create(booker.getId(), item.getId(), booking))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_withConflictingDates_shouldThrowValidationException() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(3);
        bookingService.create(booker.getId(), item.getId(), newBooking(start, end));

        Booking conflicting = newBooking(start.plusHours(12), end.plusDays(1));

        assertThatThrownBy(() -> bookingService.create(booker.getId(), item.getId(), conflicting))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void approve_byOwner_shouldSetApprovedStatus() {
        Booking saved = createBooking();

        Booking approved = bookingService.approve(owner.getId(), saved.getId(), true);

        assertThat(approved.getStatus()).isEqualTo(Status.APPROVED);
    }

    @Test
    void approve_withFalse_shouldSetRejectedStatus() {
        Booking saved = createBooking();

        Booking rejected = bookingService.approve(owner.getId(), saved.getId(), false);

        assertThat(rejected.getStatus()).isEqualTo(Status.REJECTED);
    }

    @Test
    void approve_byNotOwner_shouldThrowForbiddenException() {
        User stranger = persistUser("stranger@example.com");
        Booking saved = createBooking();

        assertThatThrownBy(() -> bookingService.approve(stranger.getId(), saved.getId(), true))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void approve_alreadyProcessed_shouldThrowValidationException() {
        Booking saved = createBooking();
        bookingService.approve(owner.getId(), saved.getId(), true);

        assertThatThrownBy(() -> bookingService.approve(owner.getId(), saved.getId(), false))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void getById_byBooker_shouldReturnBooking() {
        Booking saved = createBooking();

        Booking found = bookingService.getById(booker.getId(), saved.getId());

        assertThat(found.getId()).isEqualTo(saved.getId());
    }

    @Test
    void getById_byOwner_shouldReturnBooking() {
        Booking saved = createBooking();

        Booking found = bookingService.getById(owner.getId(), saved.getId());

        assertThat(found.getId()).isEqualTo(saved.getId());
    }

    @Test
    void getById_byStranger_shouldThrowNotFoundException() {
        User stranger = persistUser("stranger2@example.com");
        Booking saved = createBooking();

        assertThatThrownBy(() -> bookingService.getById(stranger.getId(), saved.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_withNonExistentId_shouldThrowNotFoundException() {
        assertThatThrownBy(() -> bookingService.getById(booker.getId(), 999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllByBooker_withStateAll_shouldReturnAllBookings() {
        createBooking();

        List<Booking> bookings = bookingService.getAllByBooker(booker.getId(), "ALL");

        assertThat(bookings).hasSize(1);
    }

    @Test
    void getAllByBooker_withStateWaiting_shouldReturnOnlyWaiting() {
        Booking saved = createBooking();
        bookingService.approve(owner.getId(), saved.getId(), true);

        List<Booking> waiting = bookingService.getAllByBooker(booker.getId(), "WAITING");

        assertThat(waiting).isEmpty();
    }

    @Test
    void getAllByBooker_withInvalidState_shouldThrowValidationException() {
        assertThatThrownBy(() -> bookingService.getAllByBooker(booker.getId(), "UNKNOWN"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void getAllByOwner_withStateAll_shouldReturnAllBookingsForOwnerItems() {
        createBooking();

        List<Booking> bookings = bookingService.getAllByOwner(owner.getId(), "ALL");

        assertThat(bookings).hasSize(1);
    }

    private User persistUser(String email) {
        User user = new User();
        user.setName("User " + email);
        user.setEmail(email);
        em.persist(user);
        return user;
    }

    private Item persistItem(User itemOwner, String name, boolean available) {
        Item newItem = new Item();
        newItem.setName(name);
        newItem.setDescription("Описание " + name);
        newItem.setAvailable(available);
        newItem.setOwner(itemOwner);
        em.persist(newItem);
        return newItem;
    }

    private Booking newBooking(LocalDateTime start, LocalDateTime end) {
        Booking booking = new Booking();
        booking.setStart(start);
        booking.setEnd(end);
        return booking;
    }

    private Booking createBooking() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        return bookingService.create(booker.getId(), item.getId(), newBooking(start, end));
    }
}