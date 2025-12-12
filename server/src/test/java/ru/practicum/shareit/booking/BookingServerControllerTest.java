package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingServerController.class)
class BookingServerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_ShouldReturnBookingDto() throws Exception {
        BookingCreateDto inputDto = new BookingCreateDto();
        inputDto.setStart(LocalDateTime.now().plusHours(1));
        inputDto.setEnd(LocalDateTime.now().plusHours(2));
        inputDto.setItemId(1L);

        BookingDto expectedDto = new BookingDto();
        expectedDto.setId(1L);
        expectedDto.setStart(LocalDateTime.now().plusHours(1));
        expectedDto.setEnd(LocalDateTime.now().plusHours(2));

        when(bookingService.create(any(BookingCreateDto.class), anyLong())).thenReturn(expectedDto);

        mockMvc.perform(post("/server/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void approve_ShouldReturnApprovedBookingDto() throws Exception {
        Long bookingId = 1L;
        Boolean approved = true;

        BookingDto expectedDto = new BookingDto();
        expectedDto.setId(1L);
        expectedDto.setStatus(Status.valueOf("APPROVED"));

        when(bookingService.approve(eq(bookingId), anyLong(), eq(approved))).thenReturn(expectedDto);

        mockMvc.perform(patch("/server/bookings/{bookingId}", bookingId)
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
        expectedDto.setEnd(LocalDateTime.now().plusHours(2));

        when(bookingService.getById(eq(bookingId), anyLong())).thenReturn(expectedDto);

        mockMvc.perform(get("/server/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void getUserBookings_ShouldReturnListOfBookingDtos() throws Exception {
        BookingDto booking1 = new BookingDto();
        booking1.setId(1L);
        booking1.setStart(LocalDateTime.now().plusHours(1));

        BookingDto booking2 = new BookingDto();
        booking2.setId(2L);
        booking2.setStart(LocalDateTime.now().plusHours(2));

        List<BookingDto> expectedList = Arrays.asList(booking1, booking2);

        when(bookingService.getUserBookings(anyLong(), eq("ALL"))).thenReturn(expectedList);

        mockMvc.perform(get("/server/bookings")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    void getOwnerBookings_ShouldReturnListOfBookingDtos() throws Exception {
        BookingDto booking1 = new BookingDto();
        booking1.setId(1L);
        booking1.setStart(LocalDateTime.now().plusHours(1));

        BookingDto booking2 = new BookingDto();
        booking2.setId(2L);
        booking2.setStart(LocalDateTime.now().plusHours(2));

        List<BookingDto> expectedList = Arrays.asList(booking1, booking2);

        when(bookingService.getOwnerBookings(anyLong(), eq("ALL"))).thenReturn(expectedList);

        mockMvc.perform(get("/server/bookings/owner")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }
}