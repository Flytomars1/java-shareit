package ru.practicum.shareit.itemrequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.itemrequest.dto.ItemRequestCreateDto;
import ru.practicum.shareit.itemrequest.dto.ItemRequestDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestClient itemRequestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createRequest_ShouldReturnItemRequestDto() throws Exception {
        ItemRequestCreateDto inputDto = new ItemRequestCreateDto();
        inputDto.setDescription("Request description");

        ItemRequestDto expectedDto = new ItemRequestDto();
        expectedDto.setId(1L);
        expectedDto.setDescription("Request description");

        when(itemRequestClient.create(anyLong(), any(ItemRequestCreateDto.class))).thenReturn(ResponseEntity.ok(expectedDto));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Request description"));
    }

    @Test
    void getOwnRequests_ShouldReturnListOfItemRequestDtos() throws Exception {
        Long userId = 1L;

        ItemRequestDto request1 = new ItemRequestDto();
        request1.setId(1L);
        request1.setDescription("Request 1");

        ItemRequestDto request2 = new ItemRequestDto();
        request2.setId(2L);
        request2.setDescription("Request 2");

        when(itemRequestClient.getOwnRequests(eq(userId))).thenReturn(ResponseEntity.ok(List.of(request1, request2)));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    void getAllRequests_ShouldReturnListOfItemRequestDtos() throws Exception {
        Long userId = 1L;
        Integer from = 0;
        Integer size = 20;

        ItemRequestDto request1 = new ItemRequestDto();
        request1.setId(1L);
        request1.setDescription("Request 1");

        ItemRequestDto request2 = new ItemRequestDto();
        request2.setId(2L);
        request2.setDescription("Request 2");

        when(itemRequestClient.getAllRequests(eq(userId), eq(from), eq(size))).thenReturn(ResponseEntity.ok(List.of(request1, request2)));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    void getRequest_ShouldReturnItemRequestDto() throws Exception {
        Long userId = 1L;
        Long requestId = 1L;

        ItemRequestDto expectedDto = new ItemRequestDto();
        expectedDto.setId(1L);
        expectedDto.setDescription("Request description");

        when(itemRequestClient.getRequest(eq(userId), eq(requestId))).thenReturn(ResponseEntity.ok(expectedDto));

        mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Request description"));
    }
}