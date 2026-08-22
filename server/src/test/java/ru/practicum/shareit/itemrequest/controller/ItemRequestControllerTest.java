package ru.practicum.shareit.itemrequest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Test
    void create_shouldReturnCreatedRequest() throws Exception {
        ItemRequestCreateDto createDto = new ItemRequestCreateDto("Нужна дрель");

        ItemRequestResponseDto response = new ItemRequestResponseDto(
                1L, "Нужна дрель", LocalDateTime.now(), List.of());

        when(itemRequestService.create(1L, "Нужна дрель")).thenReturn(response);

        mockMvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Нужна дрель"));
    }

    @Test
    void getOwnRequests_shouldReturnList() throws Exception {
        ItemRequestResponseDto response = new ItemRequestResponseDto(
                1L, "Нужна дрель", LocalDateTime.now(), List.of());

        when(itemRequestService.getOwnRequests(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getAllRequests_shouldReturnList() throws Exception {
        ItemRequestResponseDto response = new ItemRequestResponseDto(
                1L, "Чужой запрос", LocalDateTime.now(), List.of());

        when(itemRequestService.getAllRequests(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/requests/all")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Чужой запрос"));
    }

    @Test
    void getById_shouldReturnRequest() throws Exception {
        ItemRequestResponseDto response = new ItemRequestResponseDto(
                1L, "Нужна дрель", LocalDateTime.now(), List.of());

        when(itemRequestService.getById(1L, 1L)).thenReturn(response);

        mockMvc.perform(get("/requests/1")
                        .header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Нужна дрель"));
    }
}
