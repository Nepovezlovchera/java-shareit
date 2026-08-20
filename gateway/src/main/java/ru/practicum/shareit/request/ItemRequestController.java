package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
public class ItemRequestController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> createRequest(@RequestHeader(USER_ID_HEADER) long requestorId,
                                                @Valid @RequestBody ItemRequestDto requestDto) {
        log.info("Creating request {}, requestorId={}", requestDto, requestorId);
        return itemRequestClient.createRequest(requestorId, requestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnRequests(@RequestHeader(USER_ID_HEADER) long requestorId) {
        log.info("Get own requests, requestorId={}", requestorId);
        return itemRequestClient.getOwnRequests(requestorId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests(@RequestHeader(USER_ID_HEADER) long userId) {
        log.info("Get all requests, userId={}", userId);
        return itemRequestClient.getAllRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequest(@RequestHeader(USER_ID_HEADER) long userId,
                                             @PathVariable long requestId) {
        log.info("Get request {}, userId={}", requestId, userId);
        return itemRequestClient.getRequest(userId, requestId);
    }
}