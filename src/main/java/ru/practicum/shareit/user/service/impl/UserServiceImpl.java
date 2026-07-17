package ru.practicum.shareit.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public List<User> getAllUsers() {
        return repository.findAll();
    }

    @Override
    public User getUserById(Long userId) {
        return repository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }

    @Override
    public User saveUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ValidationException("Email не может быть пустым");
        }
        if (repository.existsByEmail(user.getEmail(), null)) {
            throw new DuplicateEmailException("Пользователь с таким email уже существует");
        }
        return repository.save(user);
    }

    @Override
    public User updateUser(Long userId, User user) {
        User existing = getUserById(userId);

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            if (repository.existsByEmail(user.getEmail(), userId)) {
                throw new DuplicateEmailException("Пользователь с таким email уже существует");
            }
            existing.setEmail(user.getEmail());
        }
        if (user.getName() != null && !user.getName().isBlank()) {
            existing.setName(user.getName());
        }
        return repository.update(existing);
    }

    @Override
    public void deleteUser(Long userId) {
        getUserById(userId);
        repository.delete(userId);
    }
}
