package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.junit.jupiter.api.Assertions.*;

@JsonTest
class ItemDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deserializeItemDto_ShouldMapCorrectly() throws Exception {
        String json = "{\n" +
                "    \"id\": 1,\n" +
                "    \"name\": \"Item Name\",\n" +
                "    \"description\": \"Item Description\",\n" +
                "    \"available\": true,\n" +
                "    \"requestId\": 2,\n" +
                "    \"comments\": []\n" +
                "}";

        ItemDto result = objectMapper.readValue(json, ItemDto.class);

        assertEquals(1L, result.getId());
        assertEquals("Item Name", result.getName());
        assertEquals("Item Description", result.getDescription());
        assertTrue(result.getAvailable());
        assertEquals(2L, result.getRequestId());
        assertNotNull(result.getComments());
    }

    @Test
    void serializeItemDto_ShouldIncludeAllFields() throws Exception {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Item Name");
        itemDto.setDescription("Item Description");
        itemDto.setAvailable(true);
        itemDto.setRequestId(2L);

        String result = objectMapper.writeValueAsString(itemDto);

        assertTrue(result.contains("\"id\":1"));
        assertTrue(result.contains("\"name\":\"Item Name\""));
        assertTrue(result.contains("\"available\":true"));
    }
}