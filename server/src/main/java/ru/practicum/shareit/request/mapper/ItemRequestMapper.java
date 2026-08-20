package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemAnswerDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {

    public static ItemRequest toItemRequest(String description, User requestor) {
        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        return request;
    }

    public static ItemRequestResponseDto toItemRequestDto(ItemRequest request, List<Item> answers) {
        ItemRequestResponseDto dto = new ItemRequestResponseDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setCreated(request.getCreated());
        dto.setItems(answers.stream()
                .map(item -> new ItemAnswerDto(item.getId(), item.getName(), item.getOwner().getId()))
                .collect(Collectors.toList()));
        return dto;
    }
}