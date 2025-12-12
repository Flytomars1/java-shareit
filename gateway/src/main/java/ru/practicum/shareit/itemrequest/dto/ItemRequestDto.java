package ru.practicum.shareit.itemrequest.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ItemRequestDto {
    private Long id;
    private String description;
    private LocalDateTime created;
    private List<ItemRequestResponseDto> items = new ArrayList<>();
}