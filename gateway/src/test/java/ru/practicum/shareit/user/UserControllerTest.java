package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserClient userClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createUser_ShouldReturnUserDto() throws Exception {
        UserDto inputDto = new UserDto();
        inputDto.setName("John Doe");
        inputDto.setEmail("john@example.com");

        UserDto expectedDto = new UserDto();
        expectedDto.setId(1L);
        expectedDto.setName("John Doe");
        expectedDto.setEmail("john@example.com");

        when(userClient.create(any(UserDto.class))).thenReturn(ResponseEntity.ok(expectedDto));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void updateUser_ShouldReturnUpdatedUserDto() throws Exception {
        Long userId = 1L;
        UserDto inputDto = new UserDto();
        inputDto.setName("Updated Name");

        UserDto expectedDto = new UserDto();
        expectedDto.setId(1L);
        expectedDto.setName("Updated Name");

        when(userClient.update(eq(userId), anyLong(), any(UserDto.class))).thenReturn(ResponseEntity.ok(expectedDto));

        mockMvc.perform(patch("/users/{id}", userId)
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void getUser_ShouldReturnUserDto() throws Exception {
        Long userId = 1L;

        UserDto expectedDto = new UserDto();
        expectedDto.setId(1L);
        expectedDto.setName("John Doe");

        when(userClient.getUser(eq(userId))).thenReturn(ResponseEntity.ok(expectedDto));

        mockMvc.perform(get("/users/{id}", userId)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void getAllUsers_ShouldReturnListOfUserDtos() throws Exception {
        UserDto user1 = new UserDto();
        user1.setId(1L);
        user1.setName("John Doe");

        UserDto user2 = new UserDto();
        user2.setId(2L);
        user2.setName("Jane Doe");

        when(userClient.getAllUsers()).thenReturn(ResponseEntity.ok(List.of(user1, user2)));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    void deleteUser_ShouldReturnStatusOk() throws Exception {
        Long userId = 1L;

        when(userClient.delete(eq(userId), anyLong())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(delete("/users/{id}", userId)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }
}