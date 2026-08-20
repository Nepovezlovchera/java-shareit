package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.status.Status;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

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
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Test
    void create_shouldPersistBookingWithWaitingStatus() {
        User owner = createUser("owner1@example.com");
        User booker = createUser("booker1@example.com");
        Item item = createItem(owner.getId(), "Дрель", true);

        Booking booking = newBooking(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        Booking created = bookingService.create(booker.getId(), item.getId(), booking);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getStatus()).isEqualTo(Status.WAITING);
        assertThat(created.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(created.getItem().getId()).isEqualTo(item.getId());
    }

    @Test
    void create_withUnavailableItem_shouldThrowValidationException() {
        User owner = createUser("owner2@example.com");
        User booker = createUser("booker2@example.com");
        Item item = createItem(owner.getId(), "Дрель", false);

        Booking booking = newBooking(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.create(booker.getId(), item.getId(), booking))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_byOwnerHimself_shouldThrowNotFoundException() {
        User owner = createUser("owner3@example.com");
        Item item = createItem(owner.getId(), "Дрель", true);

        Booking booking = newBooking(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.create(owner.getId(), item.getId(), booking))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_withEndBeforeStart_shouldThrowValidationException() {
        User owner = createUser("owner4@example.com");
        User booker = createUser("booker4@example.com");
        Item item = createItem(owner.getId(), "Дрель", true);

        Booking booking = newBooking(LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(1));

        assertThatThrownBy(() -> bookingService.create(booker.getId(), item.getId(), booking))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void create_withConflictingDates_shouldThrowValidationException() {
        User owner = createUser("owner5@example.com");
        User booker = createUser("booker5@example.com");
        Item item = createItem(owner.getId(), "Дрель", true);

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(3);
        bookingService.create(booker.getId(), item.getId(), newBooking(start, end));

        Booking conflicting = newBooking(start.plusHours(12), end.plusDays(1));

        assertThatThrownBy(() -> bookingService.create(booker.getId(), item.getId(), conflicting))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void approve_byOwner_shouldSetApprovedStatus() {
        User owner = createUser("owner6@example.com");
        User booker = createUser("booker6@example.com");
        Item item = createItem(owner.getId(), "Дрель", true);
        Booking saved = createBooking(booker.getId(), item.getId());

        Booking approved = bookingService.approve(owner.getId(), saved.getId(), true);

        assertThat(approved.getStatus()).isEqualTo(Status.APPROVED);
    }

    @Test
    void approve_withFalse_shouldSetRejectedStatus() {
        User owner = createUser("owner7@example.com");
        User booker = createUser("booker7@example.com");
        Item item = createItem(owner.getId(), "Дрель", true);
        Booking saved = createBooking(booker.getId(), item.getId());

        Booking rejected = bookingService.approve(owner.getId(), saved.getId(), false);

        assertThat(rejected.getStatus()).isEqualTo(Status.REJECTED);
    }

    @Test
    void approve_byNotOwner_shouldThrowForbiddenException() {
        User owner = createUser("owner8@example.com");
        User booker = createUser("booker8@example.com");
        User stranger = createUser("stranger8@example.com");
        Item item = createItem(owner.getId(), "Дрель", true);
        Booking saved = createBooking(booker.getId(), item.getId());

        assertThatThrownBy(() -> bookingService.approve(stranger.getId(), saved.getId(), true))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void approve_alreadyProcessed_shouldThrowValidationException() {
        User owner = createUser("owner9@example.com");
        User booker = createUser("booker9@example.com");
        Item item = createItem(owner.getId(), "Дрель", true);
        Booking saved = createBooking(booker.getId(), item.getId());
        bookingService.approve(owner.getId(), saved.getId(), true);

        assertThatThrownBy(() -> bookingService.approve(owner.getId(), saved.getId(), false))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void getById_byBooker_shouldReturnBooking() {
        User owner = createUser("owner10@example.com");
        User booker = createUser("booker10@example.com");
        Item item = createItem(owner.getId(), "Дрель", true);
        Booking saved = createBooking(booker.getId(), item.getId());

        Booking found = bookingService.getById(booker.getId(), saved.getId());

        assertThat(found.getId()).isEqualTo(saved.getId());
    }

    @Test
    void getById_byOwner_shouldReturnBooking() {
        User owner = createUser("owner11@example.com");
        User booker = createUser("booker11@example.com");
        Item item = createItem(owner.getId(), "Дрель", true);
        Booking saved = createBooking(booker.getId(), item.getId());

        Booking found = bookingService.getById(owner.getId(), saved.getId());

        assertThat(found.getId()).isEqualTo(saved.getId());
    }

    @Test
    void getById_byStranger_shouldThrowNotFoundException() {
        User owner = createUser("owner12@example.com");
        User booker = createUser("booker12@example.com");
        User stranger = createUser("stranger12@example.com");
        Item item = createItem(owner.getId(), "Дрель", true);
        Booking saved = createBooking(booker.getId(), item.getId());

        assertThatThrownBy(() -> bookingService.getById(stranger.getId(), saved.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_withNonExistentId_shouldThrowNotFoundException() {
        User user = createUser("user13@example.com");

        assertThatThrownBy(() -> bookingService.getById(user.getId(), 999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllByBooker_withStateAll_shouldReturnAllBookings() {
        User owner = createUser("owner14@example.com");
        User booker = createUser("booker14@example.com");
        Item item = createItem(owner.getId(), "Дрель", true);
        createBooking(booker.getId(), item.getId());

        List<Booking> bookings = bookingService.getAllByBooker(booker.getId(), "ALL");

        assertThat(bookings).hasSize(1);
    }

    @Test
    void getAllByBooker_withStateWaiting_shouldReturnOnlyWaiting() {
        User owner = createUser("owner15@example.com");
        User booker = createUser("booker15@example.com");
        Item item = createItem(owner.getId(), "Дрель", true);
        Booking saved = createBooking(booker.getId(), item.getId());
        bookingService.approve(owner.getId(), saved.getId(), true);

        List<Booking> waiting = bookingService.getAllByBooker(booker.getId(), "WAITING");

        assertThat(waiting).isEmpty();
    }

    @Test
    void getAllByBooker_withInvalidState_shouldThrowValidationException() {
        User booker = createUser("booker16@example.com");

        assertThatThrownBy(() -> bookingService.getAllByBooker(booker.getId(), "UNKNOWN"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void getAllByOwner_withStateAll_shouldReturnAllBookingsForOwnerItems() {
        User owner = createUser("owner17@example.com");
        User booker = createUser("booker17@example.com");
        Item item = createItem(owner.getId(), "Дрель", true);
        createBooking(booker.getId(), item.getId());

        List<Booking> bookings = bookingService.getAllByOwner(owner.getId(), "ALL");

        assertThat(bookings).hasSize(1);
    }

    private User createUser(String email) {
        User user = new User();
        user.setName("User " + email);
        user.setEmail(email);
        return userService.saveUser(user);
    }

    private Item createItem(Long ownerId, String name, boolean available) {
        Item item = new Item();
        item.setName(name);
        item.setDescription("Описание " + name);
        item.setAvailable(available);
        return itemService.createItem(ownerId, item, null);
    }

    private Booking newBooking(LocalDateTime start, LocalDateTime end) {
        Booking booking = new Booking();
        booking.setStart(start);
        booking.setEnd(end);
        return booking;
    }

    private Booking createBooking(Long bookerId, Long itemId) {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        return bookingService.create(bookerId, itemId, newBooking(start, end));
    }
}