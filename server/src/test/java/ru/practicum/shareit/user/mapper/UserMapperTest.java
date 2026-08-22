package ru.practicum.shareit.user.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    @Test
    void toUserDto_shouldMapAllFields() {
        User user = new User();
        user.setId(1L);
        user.setName("Иван");
        user.setEmail("ivan@example.com");

        UserDto dto = UserMapper.toUserDto(user);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Иван");
        assertThat(dto.getEmail()).isEqualTo("ivan@example.com");
    }

    @Test
    void toUser_shouldMapAllFields() {
        UserDto dto = new UserDto();
        dto.setId(2L);
        dto.setName("Мария");
        dto.setEmail("maria@example.com");

        User user = UserMapper.toUser(dto);

        assertThat(user.getId()).isEqualTo(2L);
        assertThat(user.getName()).isEqualTo("Мария");
        assertThat(user.getEmail()).isEqualTo("maria@example.com");
    }
}