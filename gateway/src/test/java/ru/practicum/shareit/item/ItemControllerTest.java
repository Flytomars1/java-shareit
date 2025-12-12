package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemClient itemClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_ShouldReturnItemDto() throws Exception {
        ItemDto inputDto = new ItemDto();
        inputDto.setName("Item Name");
        inputDto.setDescription("Item Description");
        inputDto.setAvailable(true);

        ItemDto expectedDto = new ItemDto();
        expectedDto.setId(1L);
        expectedDto.setName("Item Name");

        when(itemClient.create(anyLong(), any(ItemDto.class))).thenReturn(ResponseEntity.ok(expectedDto));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Item Name"));
    }

    @Test
    void update_ShouldReturnUpdatedItemDto() throws Exception {
        Long itemId = 1L;
        Long userId = 1L;
        ItemDto inputDto = new ItemDto();
        inputDto.setName("Updated Name");

        ItemDto expectedDto = new ItemDto();
        expectedDto.setId(1L);
        expectedDto.setName("Updated Name");

        when(itemClient.update(eq(userId), eq(itemId), any(ItemDto.class))).thenReturn(ResponseEntity.ok(expectedDto));

        mockMvc.perform(patch("/items/{id}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void getItem_ShouldReturnItemDto() throws Exception {
        Long itemId = 1L;
        Long userId = 1L;

        ItemDto expectedDto = new ItemDto();
        expectedDto.setId(1L);
        expectedDto.setName("Item Name");

        when(itemClient.getItem(eq(itemId), eq(userId))).thenReturn(ResponseEntity.ok(expectedDto));

        mockMvc.perform(get("/items/{id}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Item Name"));
    }

    @Test
    void getItems_ShouldReturnListOfItemDtos() throws Exception {
        Long userId = 1L;

        ItemDto item1 = new ItemDto();
        item1.setId(1L);
        item1.setName("Item 1");

        ItemDto item2 = new ItemDto();
        item2.setId(2L);
        item2.setName("Item 2");

        when(itemClient.getItems(eq(userId))).thenReturn(ResponseEntity.ok(List.of(item1, item2)));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    void search_ShouldReturnListOfItemDtos() throws Exception {
        String text = "search";

        ItemDto item1 = new ItemDto();
        item1.setId(1L);
        item1.setName("Search Item 1");

        ItemDto item2 = new ItemDto();
        item2.setId(2L);
        item2.setName("Search Item 2");

        when(itemClient.search(eq(text))).thenReturn(ResponseEntity.ok(List.of(item1, item2)));

        mockMvc.perform(get("/items/search")
                        .param("text", text))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    void addComment_ShouldReturnCommentDto() throws Exception {
        Long itemId = 1L;
        Long userId = 1L;
        CommentCreateDto inputDto = new CommentCreateDto();
        inputDto.setText("Great item!");

        when(itemClient.addComment(eq(userId), eq(itemId), String.valueOf(any(CommentCreateDto.class)))).thenReturn(ResponseEntity.ok(null));

        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk());
    }
}