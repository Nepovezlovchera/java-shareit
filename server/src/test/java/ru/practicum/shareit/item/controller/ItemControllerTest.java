package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.dto.CommentRequestDto;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void createItem_shouldReturnCreatedItem() throws Exception {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setName("Дрель");
        requestDto.setDescription("Мощная дрель");
        requestDto.setAvailable(true);
        requestDto.setRequestId(null);

        Item created = new Item();
        created.setId(1L);
        created.setName("Дрель");
        created.setDescription("Мощная дрель");
        created.setAvailable(true);

        when(itemService.createItem(eq(1L), any(Item.class), isNull()))
                .thenReturn(created);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"))
                .andExpect(jsonPath("$.description").value("Мощная дрель"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void updateItem_shouldReturnUpdatedItem() throws Exception {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setName("Новая дрель");
        requestDto.setDescription("Новое описание");
        requestDto.setAvailable(false);

        Item updated = new Item();
        updated.setId(1L);
        updated.setName("Новая дрель");
        updated.setDescription("Новое описание");
        updated.setAvailable(false);

        when(itemService.updateItem(eq(1L), eq(1L), any(Item.class)))
                .thenReturn(updated);

        mockMvc.perform(patch("/items/1")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Новая дрель"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void getItemById_shouldReturnItemWithBookings() throws Exception {
        ItemWithBookingsDto dto = new ItemWithBookingsDto();
        dto.setId(1L);
        dto.setName("Дрель");
        dto.setDescription("Описание");
        dto.setAvailable(true);
        dto.setComments(List.of());

        when(itemService.getItemWithBookings(1L, 1L)).thenReturn(dto);

        mockMvc.perform(get("/items/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    void getAllItemsByOwner_shouldReturnList() throws Exception {
        Item item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Описание");
        item.setAvailable(true);

        when(itemService.getAllItemsByOwner(1L)).thenReturn(List.of(item));

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }

    @Test
    void search_shouldReturnMatchingItems() throws Exception {
        Item item = new Item();
        item.setId(1L);
        item.setName("Дрель Bosch");
        item.setDescription("Мощная");
        item.setAvailable(true);

        when(itemService.searchItems("дрель")).thenReturn(List.of(item));

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Дрель Bosch"));
    }

    @Test
    void search_withBlankText_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/items/search")
                        .param("text", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void addComment_shouldReturnComment() throws Exception {
        CommentRequestDto requestDto = new CommentRequestDto();
        requestDto.setText("Отличная вещь");

        CommentDto commentDto = new CommentDto(
                1L,
                "Отличная вещь",
                "Иван",
                LocalDateTime.now()
        );

        when(itemService.addComment(1L, 1L, "Отличная вещь"))
                .thenReturn(commentDto);

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Отличная вещь"))
                .andExpect(jsonPath("$.authorName").value("Иван"));
    }
}