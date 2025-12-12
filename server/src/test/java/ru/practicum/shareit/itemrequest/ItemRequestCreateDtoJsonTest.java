package ru.practicum.shareit.itemrequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.itemrequest.dto.ItemRequestCreateDto;

import static org.junit.jupiter.api.Assertions.*;

@JsonTest
class ItemRequestCreateDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deserializeItemRequestCreateDto_ShouldMapCorrectly() throws Exception {
        String json = """
                {
                    "description": "Request description"
                }
                """;

        ItemRequestCreateDto result = objectMapper.readValue(json, ItemRequestCreateDto.class);

        assertEquals("Request description", result.getDescription());
    }

    @Test
    void serializeItemRequestCreateDto_ShouldIncludeAllFields() throws Exception {
        ItemRequestCreateDto requestDto = new ItemRequestCreateDto();
        requestDto.setDescription("Request description");

        String result = objectMapper.writeValueAsString(requestDto);

        assertTrue(result.contains("\"description\":\"Request description\""));
    }
}