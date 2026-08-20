package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.comment.dto.CommentRequestDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentRequestDtoJsonTest {

    @Autowired
    private JacksonTester<CommentRequestDto> json;

    @Test
    void testSerialize() throws Exception {
        CommentRequestDto dto = new CommentRequestDto("Отличная вещь");

        var result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("Отличная вещь");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = """
                {"text": "Отличная вещь"}
                """;

        CommentRequestDto dto = json.parseObject(content);

        assertThat(dto.getText()).isEqualTo("Отличная вещь");
    }
}