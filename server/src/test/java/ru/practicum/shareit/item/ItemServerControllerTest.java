package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemServerController.class)
class ItemServerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

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
        expectedDto.setDescription("Item Description");
        expectedDto.setAvailable(true);

        when(itemService.create(any(ItemDto.class), anyLong())).thenReturn(expectedDto);

        mockMvc.perform(post("/server/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Item Name"))
                .andExpect(jsonPath("$.description").value("Item Description"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void update_ShouldReturnUpdatedItemDto() throws Exception {
        Long itemId = 1L;
        ItemDto inputDto = new ItemDto();
        inputDto.setName("Updated Name");
        inputDto.setDescription("Updated Description");
        inputDto.setAvailable(false);

        ItemDto expectedDto = new ItemDto();
        expectedDto.setId(1L);
        expectedDto.setName("Updated Name");
        expectedDto.setDescription("Updated Description");
        expectedDto.setAvailable(false);

        when(itemService.update(eq(itemId), any(ItemDto.class), anyLong())).thenReturn(expectedDto);

        mockMvc.perform(patch("/server/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void getItem_ShouldReturnItemDto() throws Exception {
        Long itemId = 1L;
        ItemDto expectedDto = new ItemDto();
        expectedDto.setId(1L);
        expectedDto.setName("Item Name");
        expectedDto.setDescription("Item Description");
        expectedDto.setAvailable(true);

        when(itemService.findById(eq(itemId), anyLong())).thenReturn(expectedDto);

        mockMvc.perform(get("/server/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Item Name"))
                .andExpect(jsonPath("$.description").value("Item Description"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void getOwnerItems_ShouldReturnListOfItemDtos() throws Exception {
        ItemDto item1 = new ItemDto();
        item1.setId(1L);
        item1.setName("Item 1");
        item1.setAvailable(true);

        ItemDto item2 = new ItemDto();
        item2.setId(2L);
        item2.setName("Item 2");
        item2.setAvailable(false);

        List<ItemDto> expectedList = Arrays.asList(item1, item2);

        when(itemService.getOwnerItems(anyLong())).thenReturn(expectedList);

        mockMvc.perform(get("/server/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Item 1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Item 2"));
    }

    @Test
    void searchItems_ShouldReturnListOfItemDtos() throws Exception {
        ItemDto item1 = new ItemDto();
        item1.setId(1L);
        item1.setName("Search Item 1");
        item1.setAvailable(true);

        ItemDto item2 = new ItemDto();
        item2.setId(2L);
        item2.setName("Search Item 2");
        item2.setAvailable(false);

        List<ItemDto> expectedList = Arrays.asList(item1, item2);

        when(itemService.search("search")).thenReturn(expectedList);

        mockMvc.perform(get("/server/items/search")
                        .param("text", "search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Search Item 1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Search Item 2"));
    }

    @Test
    void addComment_ShouldReturnCommentDto() throws Exception {
        Long itemId = 1L;
        Map<String, String> commentRequest = new HashMap<>();
        commentRequest.put("text", "Great item!");

        CommentDto expectedComment = new CommentDto();
        expectedComment.setId(1L);
        expectedComment.setText("Great item!");
        expectedComment.setAuthorName("Author Name");
        expectedComment.setCreated(LocalDateTime.now());

        when(itemService.addComment(eq(itemId), anyLong(), eq("Great item!"))).thenReturn(expectedComment);

        mockMvc.perform(post("/server/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Great item!"))
                .andExpect(jsonPath("$.authorName").value("Author Name"));
    }
}