package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Test
    void saveUser_shouldPersistAndReturnUserWithId() {
        User saved = createAndSaveUser("Иван Иванов", "ivan@example.com");

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Иван Иванов");
        assertThat(saved.getEmail()).isEqualTo("ivan@example.com");
    }

    @Test
    void saveUser_withDuplicateEmail_shouldThrowException() {
        createAndSaveUser("Иван", "dup@example.com");

        User user2 = newUser("Пётр", "dup@example.com");

        assertThatThrownBy(() -> userService.saveUser(user2))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void getUserById_shouldReturnUser() {
        User saved = createAndSaveUser("Мария", "maria@example.com");

        User found = userService.getUserById(saved.getId());

        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getName()).isEqualTo("Мария");
    }

    @Test
    void getUserById_withNonExistentId_shouldThrowNotFoundException() {
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllUsers_shouldReturnAllSavedUsers() {
        createAndSaveUser("Анна", "anna@example.com");
        createAndSaveUser("Борис", "boris@example.com");

        List<User> users = userService.getAllUsers();

        assertThat(users).hasSize(2);
    }

    @Test
    void updateUser_shouldUpdateNameAndEmail() {
        User saved = createAndSaveUser("Старое имя", "old@example.com");

        User update = newUser("Новое имя", "new@example.com");
        User updated = userService.updateUser(saved.getId(), update);

        assertThat(updated.getName()).isEqualTo("Новое имя");
        assertThat(updated.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void updateUser_withPartialData_shouldUpdateOnlyProvidedFields() {
        User saved = createAndSaveUser("Имя", "keep@example.com");

        User update = new User();
        update.setName("Новое имя");
        // email не передан

        User updated = userService.updateUser(saved.getId(), update);

        assertThat(updated.getName()).isEqualTo("Новое имя");
        assertThat(updated.getEmail()).isEqualTo("keep@example.com");
    }

    @Test
    void deleteUser_shouldRemoveUser() {
        User saved = createAndSaveUser("На удаление", "delete@example.com");

        userService.deleteUser(saved.getId());

        assertThatThrownBy(() -> userService.getUserById(saved.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    private User newUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private User createAndSaveUser(String name, String email) {
        return userService.saveUser(newUser(name, email));
    }
}