package ru.practicum.shareit.request.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public ItemRequestResponseDto create(Long requestorId, String description) {
        User requestor = userRepository.findById(requestorId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + requestorId + " не найден"));

        ItemRequest request = ItemRequestMapper.toItemRequest(description, requestor);
        ItemRequest saved = itemRequestRepository.save(request);

        return ItemRequestMapper.toItemRequestDto(saved, List.of());
    }

    @Override
    public List<ItemRequestResponseDto> getOwnRequests(Long requestorId) {
        userRepository.findById(requestorId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + requestorId + " не найден"));

        List<ItemRequest> requests = itemRequestRepository.findByRequestor_IdOrderByCreatedDesc(requestorId);
        return enrichWithAnswers(requests);
    }

    @Override
    public List<ItemRequestResponseDto> getAllRequests(Long userId) {
        List<ItemRequest> requests = itemRequestRepository.findByRequestor_IdNotOrderByCreatedDesc(userId);
        return enrichWithAnswers(requests);
    }

    @Override
    public ItemRequestResponseDto getById(Long userId, Long requestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id=" + requestId + " не найден"));

        List<Item> answers = itemRepository.findByRequest_Id(requestId);
        return ItemRequestMapper.toItemRequestDto(request, answers);
    }

    private List<ItemRequestResponseDto> enrichWithAnswers(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }

        List<Long> requestIds = requests.stream().map(ItemRequest::getId).collect(Collectors.toList());
        List<Item> allAnswers = itemRepository.findByRequest_IdIn(requestIds);

        Map<Long, List<Item>> answersByRequestId = allAnswers.stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return requests.stream()
                .map(request -> ItemRequestMapper.toItemRequestDto(
                        request, answersByRequestId.getOrDefault(request.getId(), List.of())))
                .collect(Collectors.toList());
    }
}