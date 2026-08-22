package ru.practicum.shareit.itemrequest.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemRequestMapperTest {

    @Test
    void toItemRequest_shouldSetDescriptionRequestorAndCreated() {
        User requestor = new User();
        requestor.setId(1L);

        ItemRequest request = ItemRequestMapper.toItemRequest("Нужна дрель", requestor);

        assertThat(request.getDescription()).isEqualTo("Нужна дрель");
        assertThat(request.getRequestor()).isEqualTo(requestor);
        assertThat(request.getCreated()).isNotNull();
        assertThat(request.getCreated()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void toItemRequestDto_withoutAnswers_shouldMapEmptyItemsList() {
        User requestor = new User();
        requestor.setId(1L);

        ItemRequest request = new ItemRequest();
        request.setId(5L);
        request.setDescription("Нужна дрель");
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        ItemRequestResponseDto dto = ItemRequestMapper.toItemRequestDto(request, List.of());

        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getDescription()).isEqualTo("Нужна дрель");
        assertThat(dto.getCreated()).isEqualTo(request.getCreated());
        assertThat(dto.getItems()).isEmpty();
    }

    @Test
    void toItemRequestDto_withAnswers_shouldMapItemAnswerDtoList() {
        User requestor = new User();
        requestor.setId(1L);

        User owner = new User();
        owner.setId(2L);

        ItemRequest request = new ItemRequest();
        request.setId(5L);
        request.setDescription("Нужна дрель");
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        Item item = new Item();
        item.setId(10L);
        item.setName("Дрель");
        item.setOwner(owner);

        ItemRequestResponseDto dto = ItemRequestMapper.toItemRequestDto(request, List.of(item));

        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getId()).isEqualTo(10L);
        assertThat(dto.getItems().get(0).getName()).isEqualTo("Дрель");
        assertThat(dto.getItems().get(0).getOwnerId()).isEqualTo(2L);
    }
}
