package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CommentMapperTest {

    @Test
    void toCommentDto_shouldMapAllFields() {
        User author = new User();
        author.setId(1L);
        author.setName("Иван");

        LocalDateTime created = LocalDateTime.now();

        Comment comment = new Comment();
        comment.setId(10L);
        comment.setText("Отличная вещь");
        comment.setAuthor(author);
        comment.setCreated(created);

        CommentDto dto = CommentMapper.toCommentDto(comment);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getText()).isEqualTo("Отличная вещь");
        assertThat(dto.getAuthorName()).isEqualTo("Иван");
        assertThat(dto.getCreated()).isEqualTo(created);
    }

    @Test
    void toComment_shouldMapTextItemAndAuthorAndSetCreated() {
        Item item = new Item();
        item.setId(2L);

        User author = new User();
        author.setId(3L);

        Comment comment = CommentMapper.toComment("Хорошая вещь", item, author);

        assertThat(comment.getText()).isEqualTo("Хорошая вещь");
        assertThat(comment.getItem()).isEqualTo(item);
        assertThat(comment.getAuthor()).isEqualTo(author);
        assertThat(comment.getCreated()).isNotNull();
        assertThat(comment.getCreated()).isBeforeOrEqualTo(LocalDateTime.now());
    }
}
