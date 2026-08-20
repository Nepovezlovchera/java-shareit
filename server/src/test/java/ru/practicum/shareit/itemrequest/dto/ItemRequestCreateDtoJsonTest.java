package ru.practicum.shareit.itemrequest.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestCreateDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestCreateDto> json;

    @Test
    void testSerialize() throws Exception {
        ItemRequestCreateDto dto = new ItemRequestCreateDto("Нужна дрель");

        var result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Нужна дрель");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = """
                {
                  "description": "Нужна дрель"
                }
                """;

        ItemRequestCreateDto dto = json.parseObject(content);

        assertThat(dto.getDescription()).isEqualTo("Нужна дрель");
    }
}
