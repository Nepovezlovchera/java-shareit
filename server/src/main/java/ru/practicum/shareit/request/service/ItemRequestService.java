package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestResponseDto create(Long requestorId, String description);

    List<ItemRequestResponseDto> getOwnRequests(Long requestorId);

    List<ItemRequestResponseDto> getAllRequests(Long userId);

    ItemRequestResponseDto getById(Long userId, Long requestId);
}