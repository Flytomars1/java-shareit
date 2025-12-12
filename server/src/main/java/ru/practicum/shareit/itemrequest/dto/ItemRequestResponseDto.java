package ru.practicum.shareit.itemrequest.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemRequestResponseDto {
    private Long id;
    private String name;
    private Long ownerId;
}