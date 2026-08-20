package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ItemServiceImplTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Test
    void createItem_shouldPersistAndReturnItemWithId() {
        User owner = createAndSaveUser("Владелец", "owner@example.com");
        Item item = newItem("Дрель", "Мощная дрель", true);

        Item created = itemService.createItem(owner.getId(), item, null);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Дрель");
        assertThat(created.getOwner().getId()).isEqualTo(owner.getId());
    }

    @Test
    void createItem_withNonExistentOwner_shouldThrowNotFoundException() {
        Item item = newItem("Дрель", "Мощная дрель", true);

        assertThatThrownBy(() -> itemService.createItem(999L, item, null))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateItem_byOwner_shouldUpdateFields() {
        User owner = createAndSaveUser("Владелец", "owner2@example.com");
        Item saved = createAndSaveItem(owner.getId(), "Дрель", "Старое описание", true);

        Item update = newItem(null, "Новое описание", false);
        Item updated = itemService.updateItem(owner.getId(), saved.getId(), update);

        assertThat(updated.getDescription()).isEqualTo("Новое описание");
        assertThat(updated.getAvailable()).isFalse();
        assertThat(updated.getName()).isEqualTo("Дрель");
    }

    @Test
    void updateItem_byNotOwner_shouldThrowForbiddenException() {
        User owner = createAndSaveUser("Владелец", "owner3@example.com");
        User stranger = createAndSaveUser("Чужой", "stranger@example.com");
        Item saved = createAndSaveItem(owner.getId(), "Дрель", "Описание", true);

        Item update = newItem("Новое имя", null, null);

        assertThatThrownBy(() -> itemService.updateItem(stranger.getId(), saved.getId(), update))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getItemById_shouldReturnItem() {
        User owner = createAndSaveUser("Владелец", "owner4@example.com");
        Item saved = createAndSaveItem(owner.getId(), "Дрель", "Описание", true);

        Item found = itemService.getItemById(saved.getId());

        assertThat(found.getId()).isEqualTo(saved.getId());
    }

    @Test
    void getItemById_withNonExistentId_shouldThrowNotFoundException() {
        assertThatThrownBy(() -> itemService.getItemById(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllItemsByOwner_shouldReturnOnlyOwnerItems() {
        User owner = createAndSaveUser("Владелец", "owner5@example.com");
        User otherOwner = createAndSaveUser("Другой владелец", "owner6@example.com");
        createAndSaveItem(owner.getId(), "Дрель", "Описание", true);
        createAndSaveItem(owner.getId(), "Молоток", "Описание", true);
        createAndSaveItem(otherOwner.getId(), "Пила", "Описание", true);

        List<Item> items = itemService.getAllItemsByOwner(owner.getId());

        assertThat(items).hasSize(2);
    }

    @Test
    void searchItems_shouldReturnOnlyAvailableMatchingItems() {
        User owner = createAndSaveUser("Владелец", "owner7@example.com");
        createAndSaveItem(owner.getId(), "Дрель Bosch", "Мощная дрель", true);
        createAndSaveItem(owner.getId(), "Дрель Makita", "Недоступна", false);
        createAndSaveItem(owner.getId(), "Молоток", "Обычный", true);

        List<Item> found = itemService.searchItems("дрель");

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Дрель Bosch");
    }

    @Test
    void searchItems_withBlankText_shouldReturnEmptyList() {
        List<Item> found = itemService.searchItems("");

        assertThat(found).isEmpty();
    }

    @Test
    void addComment_withoutCompletedBooking_shouldThrowValidationException() {
        User owner = createAndSaveUser("Владелец", "owner8@example.com");
        User author = createAndSaveUser("Автор", "author@example.com");
        Item item = createAndSaveItem(owner.getId(), "Дрель", "Описание", true);

        assertThatThrownBy(() -> itemService.addComment(author.getId(), item.getId(), "Отличная вещь"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void getItemWithBookings_asNotOwner_shouldNotShowBookingDates() {
        User owner = createAndSaveUser("Владелец", "owner9@example.com");
        User stranger = createAndSaveUser("Чужой", "stranger2@example.com");
        Item item = createAndSaveItem(owner.getId(), "Дрель", "Описание", true);

        ItemWithBookingsDto dto = itemService.getItemWithBookings(item.getId(), stranger.getId());

        assertThat(dto.getLastBooking()).isNull();
        assertThat(dto.getNextBooking()).isNull();
        assertThat(dto.getComments()).isEmpty();
    }

    @Test
    void getAllItemsByOwnerWithBookings_shouldReturnItemsWithEmptyBookingsAndComments() {
        User owner = createAndSaveUser("Владелец", "owner10@example.com");
        createAndSaveItem(owner.getId(), "Дрель", "Описание", true);

        List<ItemWithBookingsDto> items = itemService.getAllItemsByOwnerWithBookings(owner.getId());

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getComments()).isEmpty();
    }

    private User createAndSaveUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return userService.saveUser(user);
    }

    private Item newItem(String name, String description, Boolean available) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(available);
        return item;
    }

    private Item createAndSaveItem(Long ownerId, String name, String description, Boolean available) {
        return itemService.createItem(ownerId, newItem(name, description, available), null);
    }
}