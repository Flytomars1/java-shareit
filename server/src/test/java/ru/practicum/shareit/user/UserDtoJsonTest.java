package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.*;

@JsonTest
class UserDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deserializeUserDto_ShouldMapCorrectly() throws Exception {
        String json = """
                {
                    "id": 1,
                    "name": "John Doe",
                    "email": "john@example.com"
                }
                """;

        UserDto result = objectMapper.readValue(json, UserDto.class);

        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
    }

    @Test
    void serializeUserDto_ShouldIncludeAllFields() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("John Doe");
        userDto.setEmail("john@example.com");

        String result = objectMapper.writeValueAsString(userDto);

        assertTrue(result.contains("\"id\":1"));
        assertTrue(result.contains("\"name\":\"John Doe\""));
        assertTrue(result.contains("\"email\":\"john@example.com\""));
    }
}