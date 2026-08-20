package ru.practicum.shareit.book.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingRequestDtoJsonTest {

    @Autowired
    private JacksonTester<BookingRequestDto> json;

    @Test
    void testSerialize() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 8, 21, 12, 0);
        LocalDateTime end = LocalDateTime.of(2026, 8, 22, 12, 0);
        BookingRequestDto dto = new BookingRequestDto(1L, start, end);

        var result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2026-08-21T12:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2026-08-22T12:00:00");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"itemId\": 1, \"start\": \"2026-08-21T12:00:00\", \"end\": \"2026-08-22T12:00:00\"}";

        BookingRequestDto dto = json.parseObject(content);

        assertThat(dto.getItemId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2026, 8, 21, 12, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2026, 8, 22, 12, 0));
    }
}
