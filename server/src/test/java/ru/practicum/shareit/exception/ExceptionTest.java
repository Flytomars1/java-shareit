package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    @Test
    void validationException_ShouldCreateWithMessage() {
        ValidationException exception = new ValidationException("Test message");
        assertEquals("Test message", exception.getMessage());
    }

    @Test
    void notFoundException_ShouldCreateWithMessage() {
        NotFoundException exception = new NotFoundException("Test message");
        assertEquals("Test message", exception.getMessage());
    }

    @Test
    void accessDeniedException_ShouldCreateWithMessage() {
        AccessDeniedException exception = new AccessDeniedException("Test message");
        assertEquals("Test message", exception.getMessage());
    }

    @Test
    void emailAlreadyExistsException_ShouldCreateWithMessage() {
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException("test@example.com");
        assertTrue(exception.getMessage().contains("test@example.com"));
    }
}