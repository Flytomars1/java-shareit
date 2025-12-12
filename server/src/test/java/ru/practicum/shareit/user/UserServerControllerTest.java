package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserServerController.class)
class UserServerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_ShouldReturnUserDto() throws Exception {
        UserDto inputDto = new UserDto(null, "John Doe", "john@example.com");
        UserDto expectedDto = new UserDto(1L, "John Doe", "john@example.com");

        when(userService.createUser(any(UserDto.class))).thenReturn(expectedDto);

        mockMvc.perform(post("/server/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(userService).createUser(any(UserDto.class));
    }

    @Test
    void update_ShouldReturnUpdatedUserDto() throws Exception {
        Long userId = 1L;
        UserDto inputDto = new UserDto(null, "Updated Name", "updated@example.com");
        UserDto expectedDto = new UserDto(1L, "Updated Name", "updated@example.com");

        when(userService.updateUser(anyLong(), any(UserDto.class))).thenReturn(expectedDto);

        mockMvc.perform(patch("/server/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));

        verify(userService).updateUser(eq(userId), any(UserDto.class));
    }

    @Test
    void getUser_ShouldReturnUserDto() throws Exception {
        Long userId = 1L;
        UserDto expectedDto = new UserDto(1L, "John Doe", "john@example.com");

        when(userService.getUserById(anyLong())).thenReturn(expectedDto);

        mockMvc.perform(get("/server/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(userService).getUserById(eq(userId));
    }

    @Test
    void getAllUsers_ShouldReturnListOfUserDtos() throws Exception {
        UserDto user1 = new UserDto(1L, "John Doe", "john@example.com");
        UserDto user2 = new UserDto(2L, "Jane Doe", "jane@example.com");
        List<UserDto> expectedList = Arrays.asList(user1, user2);

        when(userService.getAllUsers()).thenReturn(expectedList);

        mockMvc.perform(get("/server/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Jane Doe"));

        verify(userService).getAllUsers();
    }

    @Test
    void deleteUser_ShouldReturnStatusOk() throws Exception {
        Long userId = 1L;

        mockMvc.perform(delete("/server/users/{id}", userId))
                .andExpect(status().isOk());

        verify(userService).deleteUser(eq(userId));
    }
}