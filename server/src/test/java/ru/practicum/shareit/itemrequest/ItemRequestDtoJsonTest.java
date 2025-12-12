package ru.practicum.shareit.itemrequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.itemrequest.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deserializeItemRequestDto_ShouldMapCorrectly() throws Exception {
        String json = "{\n" +
                "    \"id\": 1,\n" +
                "    \"description\": \"Request description\",\n" +
                "    \"created\": \"2023-12-12T10:00:00\",\n" +
                "    \"items\": []\n" +
                "}";

        ItemRequestDto result = objectMapper.readValue(json, ItemRequestDto.class);

        assertEquals(1L, result.getId());
        assertEquals("Request description", result.getDescription());
        assertNotNull(result.getCreated());
        assertNotNull(result.getItems());
    }

    @Test
    void serializeItemRequestDto_ShouldIncludeAllFields() throws Exception {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setId(1L);
        requestDto.setDescription("Request description");
        requestDto.setCreated(LocalDateTime.now());
        requestDto.setItems(List.of());

        String result = objectMapper.writeValueAsString(requestDto);

        assertTrue(result.contains("\"id\":1"));
        assertTrue(result.contains("\"description\":\"Request description\""));
    }
}