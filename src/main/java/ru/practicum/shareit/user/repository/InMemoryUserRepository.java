package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private final Map<String, Long> emailToUserId = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User save(User user) {
        user.setId(idCounter.incrementAndGet());
        users.put(user.getId(), user);
        emailToUserId.put(user.getEmail().toLowerCase(), user.getId());
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public User update(User newUser) {
        users.put(newUser.getId(), newUser);
        return newUser;
    }

    @Override
    public void delete(Long id) {
        users.remove(id);
    }

    @Override
    public boolean existsByEmail(String email, Long excludeUserId) {
        Long ownerId = emailToUserId.get(email.toLowerCase());
        return ownerId != null && !ownerId.equals(excludeUserId);
    }
}
