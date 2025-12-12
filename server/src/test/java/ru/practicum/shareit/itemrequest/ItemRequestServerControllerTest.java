package ru.practicum.shareit.itemrequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.itemrequest.dto.ItemRequestCreateDto;
import ru.practicum.shareit.itemrequest.dto.ItemRequestDto;
import ru.practicum.shareit.itemrequest.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestServerController.class)
class ItemRequestServerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestService itemRequestService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_ShouldReturnItemRequestDto() throws Exception {
        ItemRequestCreateDto inputDto = new ItemRequestCreateDto();
        inputDto.setDescription("Request description");

        ItemRequestDto expectedDto = new ItemRequestDto();
        expectedDto.setId(1L);
        expectedDto.setDescription("Request description");
        expectedDto.setCreated(LocalDateTime.now());

        when(itemRequestService.create(anyLong(), any(String.class))).thenReturn(expectedDto);

        mockMvc.perform(post("/server/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Request description"));
    }

    @Test
    void getOwnRequests_ShouldReturnListOfItemRequestDtos() throws Exception {
        ItemRequestDto request1 = new ItemRequestDto();
        request1.setId(1L);
        request1.setDescription("Request 1");

        ItemRequestDto request2 = new ItemRequestDto();
        request2.setId(2L);
        request2.setDescription("Request 2");

        List<ItemRequestDto> expectedList = Arrays.asList(request1, request2);

        when(itemRequestService.getOwnRequests(anyLong())).thenReturn(expectedList);

        mockMvc.perform(get("/server/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].description").value("Request 1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].description").value("Request 2"));
    }

    @Test
    void getAllRequests_ShouldReturnListOfItemRequestDtos() throws Exception {
        ItemRequestDto request1 = new ItemRequestDto();
        request1.setId(1L);
        request1.setDescription("Request 1");

        ItemRequestDto request2 = new ItemRequestDto();
        request2.setId(2L);
        request2.setDescription("Request 2");

        List<ItemRequestDto> expectedList = Arrays.asList(request1, request2);

        when(itemRequestService.getAllRequests(anyLong(), any(Integer.class), any(Integer.class))).thenReturn(expectedList);

        mockMvc.perform(get("/server/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].description").value("Request 1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].description").value("Request 2"));
    }

    @Test
    void getRequest_ShouldReturnItemRequestDto() throws Exception {
        Long requestId = 1L;
        ItemRequestDto expectedDto = new ItemRequestDto();
        expectedDto.setId(1L);
        expectedDto.setDescription("Request description");

        when(itemRequestService.getRequestById(eq(requestId), anyLong())).thenReturn(expectedDto);

        mockMvc.perform(get("/server/requests/{requestId}", requestId)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Request description"));
    }
}