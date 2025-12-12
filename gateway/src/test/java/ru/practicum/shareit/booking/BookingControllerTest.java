package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.dto.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingClient bookingClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createBooking_ShouldReturnBookingDto() throws Exception {
        BookingCreateDto inputDto = new BookingCreateDto();
        inputDto.setStart(LocalDateTime.now().plusHours(1));
        inputDto.setEnd(LocalDateTime.now().plusHours(2));
        inputDto.setItemId(1L);

        BookingDto expectedDto = new BookingDto();
        expectedDto.setId(1L);
        expectedDto.setStart(LocalDateTime.now().plusHours(1));

        when(bookingClient.bookItem(anyLong(), any(BookingCreateDto.class))).thenReturn(ResponseEntity.ok(expectedDto));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void approveBooking_ShouldReturnBookingDto() throws Exception {
        Long bookingId = 1L;
        Boolean approved = true;

        BookingDto expectedDto = new BookingDto();
        expectedDto.setId(1L);
        expectedDto.setStatus(BookingStatus.valueOf("APPROVED"));

        when(bookingClient.approveBooking(anyLong(), eq(bookingId), eq(approved))).thenReturn(ResponseEntity.ok(expectedDto));

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", approved.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getBooking_ShouldReturnBookingDto() throws Exception {
        Long bookingId = 1L;

        BookingDto expectedDto = new BookingDto();
        expectedDto.setId(1L);
        expectedDto.setStart(LocalDateTime.now().plusHours(1));

        when(bookingClient.getBooking(anyLong(), eq(bookingId))).thenReturn(ResponseEntity.ok(expectedDto));

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getUserBookings_ShouldReturnListOfBookingDtos() throws Exception {
        Long userId = 1L;

        BookingDto booking1 = new BookingDto();
        booking1.setId(1L);
        booking1.setStart(LocalDateTime.now().plusHours(1));

        BookingDto booking2 = new BookingDto();
        booking2.setId(2L);
        booking2.setStart(LocalDateTime.now().plusHours(2));

        when(bookingClient.getBookings(eq(userId), any(BookingState.class), any(Integer.class), any(Integer.class)))
                .thenReturn(ResponseEntity.ok(List.of(booking1, booking2)));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    void getOwnerBookings_ShouldReturnListOfBookingDtos() throws Exception {
        Long userId = 1L;

        BookingDto booking1 = new BookingDto();
        booking1.setId(1L);
        booking1.setStart(LocalDateTime.now().plusHours(1));

        BookingDto booking2 = new BookingDto();
        booking2.setId(2L);
        booking2.setStart(LocalDateTime.now().plusHours(2));

        when(bookingClient.getOwnerBookings(eq(userId), any(BookingState.class), any(Integer.class), any(Integer.class)))
                .thenReturn(ResponseEntity.ok(List.of(booking1, booking2)));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }
}