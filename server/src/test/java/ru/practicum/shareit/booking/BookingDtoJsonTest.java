package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deserializeBookingDto_ShouldMapCorrectly() throws Exception {
        String json = """
                {
                    "id": 1,
                    "start": "2023-12-12T10:00:00",
                    "end": "2023-12-12T11:00:00",
                    "item": {
                        "id": 1,
                        "name": "Item Name",
                        "description": "Item Description",
                        "available": true
                    },
                    "booker": {
                        "id": 2,
                        "name": "Booker Name",
                        "email": "booker@example.com"
                    },
                    "status": "WAITING"
                }
                """;

        BookingDto result = objectMapper.readValue(json, BookingDto.class);

        assertEquals(1L, result.getId());
        assertEquals(Status.WAITING, result.getStatus());
        assertNotNull(result.getStart());
        assertNotNull(result.getEnd());
        assertNotNull(result.getItem());
        assertNotNull(result.getBooker());
        assertEquals(1L, result.getItem().getId());
        assertEquals(2L, result.getBooker().getId());
    }

    @Test
    void serializeBookingDto_ShouldIncludeAllFields() throws Exception {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setStart(LocalDateTime.now());
        bookingDto.setEnd(LocalDateTime.now().plusHours(1));
        bookingDto.setStatus(Status.APPROVED);

        String result = objectMapper.writeValueAsString(bookingDto);

        assertTrue(result.contains("\"id\":1"));
        assertTrue(result.contains("\"status\":\"APPROVED\""));
    }
}