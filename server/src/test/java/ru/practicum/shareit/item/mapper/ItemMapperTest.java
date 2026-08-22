package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ItemMapperTest {

    @Test
    void toItemDto_withRequest_shouldMapRequestId() {
        ItemRequest request = new ItemRequest();
        request.setId(5L);

        Item item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Мощная дрель");
        item.setAvailable(true);
        item.setRequest(request);

        ItemDto dto = ItemMapper.toItemDto(item);

        assertThat(dto.getRequest()).isNotNull();
        assertThat(dto.getRequest().getId()).isEqualTo(5L);
    }
}