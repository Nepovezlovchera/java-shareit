package ru.practicum.shareit.itemrequest.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ItemRequestServiceImplTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Test
    void create_shouldPersistAndReturnRequestWithEmptyItems() {
        User requestor = createUser("requestor1@example.com");

        ItemRequestResponseDto created = itemRequestService.create(requestor.getId(), "Нужна дрель");

        assertThat(created.getId()).isNotNull();
        assertThat(created.getDescription()).isEqualTo("Нужна дрель");
        assertThat(created.getCreated()).isNotNull();
        assertThat(created.getItems()).isEmpty();
    }

    @Test
    void create_withNonExistentRequestor_shouldThrowNotFoundException() {
        assertThatThrownBy(() -> itemRequestService.create(999L, "Нужна дрель"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getOwnRequests_shouldReturnOnlyRequestorOwnRequests() {
        User requestor = createUser("requestor2@example.com");
        User other = createUser("requestor3@example.com");
        itemRequestService.create(requestor.getId(), "Запрос 1");
        itemRequestService.create(other.getId(), "Запрос 2");

        List<ItemRequestResponseDto> own = itemRequestService.getOwnRequests(requestor.getId());

        assertThat(own).hasSize(1);
        assertThat(own.get(0).getDescription()).isEqualTo("Запрос 1");
    }

    @Test
    void getOwnRequests_shouldBeOrderedFromNewestToOldest() {
        User requestor = createUser("requestor4@example.com");
        itemRequestService.create(requestor.getId(), "Первый запрос");
        itemRequestService.create(requestor.getId(), "Второй запрос");

        List<ItemRequestResponseDto> own = itemRequestService.getOwnRequests(requestor.getId());

        assertThat(own).hasSize(2);
        assertThat(own.get(0).getDescription()).isEqualTo("Второй запрос");
        assertThat(own.get(1).getDescription()).isEqualTo("Первый запрос");
    }

    @Test
    void getAllRequests_shouldExcludeOwnRequests() {
        User requestor = createUser("requestor5@example.com");
        User other = createUser("requestor6@example.com");
        itemRequestService.create(requestor.getId(), "Свой запрос");
        itemRequestService.create(other.getId(), "Чужой запрос");

        List<ItemRequestResponseDto> all = itemRequestService.getAllRequests(requestor.getId());

        assertThat(all).hasSize(1);
        assertThat(all.get(0).getDescription()).isEqualTo("Чужой запрос");
    }

    @Test
    void getById_shouldReturnRequestWithAnswers() {
        User requestor = createUser("requestor7@example.com");
        User owner = createUser("owner7@example.com");
        ItemRequestResponseDto request = itemRequestService.create(requestor.getId(), "Нужна дрель");

        Item item = new Item();
        item.setName("Дрель");
        item.setDescription("Мощная дрель");
        item.setAvailable(true);
        itemService.createItem(owner.getId(), item, request.getId());

        ItemRequestResponseDto found = itemRequestService.getById(requestor.getId(), request.getId());

        assertThat(found.getItems()).hasSize(1);
        assertThat(found.getItems().get(0).getName()).isEqualTo("Дрель");
        assertThat(found.getItems().get(0).getOwnerId()).isEqualTo(owner.getId());
    }

    @Test
    void getById_withNonExistentRequest_shouldThrowNotFoundException() {
        User user = createUser("user8@example.com");

        assertThatThrownBy(() -> itemRequestService.getById(user.getId(), 999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById_withNonExistentUser_shouldThrowNotFoundException() {
        User requestor = createUser("requestor9@example.com");
        ItemRequestResponseDto request = itemRequestService.create(requestor.getId(), "Запрос");

        assertThatThrownBy(() -> itemRequestService.getById(999L, request.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    private User createUser(String email) {
        User user = new User();
        user.setName("User " + email);
        user.setEmail(email);
        return userService.saveUser(user);
    }
}