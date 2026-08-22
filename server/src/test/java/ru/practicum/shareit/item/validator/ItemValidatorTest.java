package ru.practicum.shareit.item.validator;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.validate.ItemValidator;
import ru.practicum.shareit.user.model.User;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ItemValidatorTest {

    @Test
    void validateOwner_withCorrectOwner_shouldNotThrow() {
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setOwner(owner);

        assertThatCode(() -> ItemValidator.validateOwner(item, 1L))
                .doesNotThrowAnyException();
    }

    @Test
    void validateOwner_withWrongOwner_shouldThrowForbiddenException() {
        User owner = new User();
        owner.setId(1L);

        Item item = new Item();
        item.setOwner(owner);

        assertThatThrownBy(() -> ItemValidator.validateOwner(item, 2L))
                .isInstanceOf(ForbiddenException.class);
    }
}
