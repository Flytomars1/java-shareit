package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.booking.dto.BookingCreateDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@JsonTest
class BookingDtoCreateJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deserializeBookingCreateDto_ShouldMapCorrectly() throws Exception {
        String json = """
                {
                    "start": "2023-12-12T10:00:00",
                    "end": "2023-12-12T11:00:00",
                    "itemId": 1
                }
                """;

        BookingCreateDto result = objectMapper.readValue(json, BookingCreateDto.class);

        assertNotNull(result.getStart());
        assertNotNull(result.getEnd());
        assertEquals(1L, result.getItemId());
    }

    @Test
    void serializeBookingCreateDto_ShouldIncludeAllFields() throws Exception {
        BookingCreateDto bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setStart(LocalDateTime.now());
        bookingCreateDto.setEnd(LocalDateTime.now().plusHours(1));
        bookingCreateDto.setItemId(1L);

        String result = objectMapper.writeValueAsString(bookingCreateDto);

        assertTrue(result.contains("\"start\":"));
        assertTrue(result.contains("\"end\":"));
        assertTrue(result.contains("\"itemId\":1"));
    }
}