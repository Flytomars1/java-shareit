package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.item.dto.CommentDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@JsonTest
class CommentDtoJsonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deserializeCommentDto_ShouldMapCorrectly() throws Exception {
        String json = """
                {
                    "id": 1,
                    "text": "Great item!",
                    "authorName": "Author Name",
                    "created": "2023-12-12T10:00:00"
                }
                """;

        CommentDto result = objectMapper.readValue(json, CommentDto.class);

        assertEquals(1L, result.getId());
        assertEquals("Great item!", result.getText());
        assertEquals("Author Name", result.getAuthorName());
        assertNotNull(result.getCreated());
    }

    @Test
    void serializeCommentDto_ShouldIncludeAllFields() throws Exception {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Great item!");
        commentDto.setAuthorName("Author Name");
        commentDto.setCreated(LocalDateTime.now());

        String result = objectMapper.writeValueAsString(commentDto);

        assertTrue(result.contains("\"id\":1"));
        assertTrue(result.contains("\"text\":\"Great item!\""));
        assertTrue(result.contains("\"authorName\":\"Author Name\""));
    }
}