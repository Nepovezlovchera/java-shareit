package ru.practicum.shareit.item.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.status.Status;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.reposytory.CommentRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.validate.ItemValidator;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public Item createItem(Long ownerId, Item item, Long requestId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + ownerId + " не найден"));

        item.setOwner(owner);

        if (requestId != null) {
            ItemRequest request = itemRequestRepository.findById(requestId)
                    .orElseThrow(() -> new NotFoundException("Запрос с id=" + requestId + " не найден"));
            item.setRequest(request);
        }

        return itemRepository.save(item);
    }

    @Override
    public Item updateItem(Long ownerId, Long itemId, Item item) {
        Item existing = getItemById(itemId);

        ItemValidator.validateOwner(existing, ownerId);

        if (item.getName() != null && !item.getName().isBlank()) {
            existing.setName(item.getName());
        }
        if (item.getDescription() != null && !item.getDescription().isBlank()) {
            existing.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            existing.setAvailable(item.getAvailable());
        }

        return itemRepository.save(existing);
    }

    @Override
    public Item getItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));
    }

    @Override
    public List<Item> getAllItemsByOwner(Long ownerId) {
        return itemRepository.findAllByOwnerId(ownerId);
    }

    @Override
    public List<Item> searchItems(String text) {
        return itemRepository.search(text);
    }

    @Override
    public CommentDto addComment(Long authorId, Long itemId, String text) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + authorId + " не найден"));

        Item item = getItemById(itemId);

        validateHasCompletedBooking(authorId, itemId);

        Comment comment = CommentMapper.toComment(text, item, author);

        Comment saved = commentRepository.save(comment);
        return CommentMapper.toCommentDto(saved);
    }

    @Override
    public ItemWithBookingsDto getItemWithBookings(Long itemId, Long requesterId) {
        Item item = getItemById(itemId);

        LocalDateTime lastBooking = null;
        LocalDateTime nextBooking = null;

        if (item.getOwner().getId().equals(requesterId)) {
            List<Booking> bookings = bookingRepository.findByItem_IdOrderByStartAsc(itemId);
            LocalDateTime now = LocalDateTime.now();

            lastBooking = findLastBookingStart(bookings, now);
            nextBooking = findNextBookingStart(bookings, now);
        }

        List<CommentDto> comments = commentRepository.findByItem_Id(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());

        return ItemMapper.toItemWithBookingsDto(item, lastBooking, nextBooking, comments);
    }

    @Override
    public List<ItemWithBookingsDto> getAllItemsByOwnerWithBookings(Long ownerId) {
        List<Item> items = itemRepository.findAllByOwnerId(ownerId);
        if (items.isEmpty()) {
            return List.of();
        }

        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());

        List<Booking> allBookings = bookingRepository.findByItem_IdInOrderByStartAsc(itemIds);
        Map<Long, List<Booking>> bookingsByItemId = allBookings.stream()
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));

        List<Comment> allComments = commentRepository.findByItem_IdIn(itemIds);
        Map<Long, List<CommentDto>> commentsByItemId = allComments.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getItem().getId(),
                        Collectors.mapping(CommentMapper::toCommentDto, Collectors.toList())
                ));

        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> {
                    List<Booking> bookings = bookingsByItemId.getOrDefault(item.getId(), List.of());

                    LocalDateTime last = findLastBookingStart(bookings, now);
                    LocalDateTime next = findNextBookingStart(bookings, now);

                    List<CommentDto> comments = commentsByItemId.getOrDefault(item.getId(), List.of());

                    return ItemMapper.toItemWithBookingsDto(item, last, next, comments);
                })
                .collect(Collectors.toList());
    }

    private LocalDateTime findLastBookingStart(List<Booking> bookings, LocalDateTime now) {
        return bookings.stream()
                .filter(b -> b.getStart().isBefore(now))
                .max(Comparator.comparing(Booking::getStart))
                .map(Booking::getStart)
                .orElse(null);
    }

    private LocalDateTime findNextBookingStart(List<Booking> bookings, LocalDateTime now) {
        return bookings.stream()
                .filter(b -> b.getStart().isAfter(now))
                .min(Comparator.comparing(Booking::getStart))
                .map(Booking::getStart)
                .orElse(null);
    }

    private void validateHasCompletedBooking(Long authorId, Long itemId) {
        boolean hasCompletedBooking = bookingRepository
                .findByBooker_IdAndItem_IdAndEndBeforeAndStatus(
                        authorId, itemId, LocalDateTime.now(), Status.APPROVED)
                .stream()
                .findAny()
                .isPresent();

        if (!hasCompletedBooking) {
            throw new ValidationException("Оставить отзыв может только пользователь, завершивший аренду этой вещи");
        }
    }
}