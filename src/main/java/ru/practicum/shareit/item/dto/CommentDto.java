package ru.practicum.shareit.item.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDto {

    public CommentDto() {}

    public CommentDto(Long id, String text, String authorName, LocalDateTime created) {
        this.id = id;
        this.text = text;
        this.authorName = authorName;
        this.created = created;
    }

    private Long id;
    private String text;
    private String authorName;
    private LocalDateTime created;
}