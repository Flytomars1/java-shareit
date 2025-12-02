package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByItemIdOrderById(Long itemId);

    @Query("SELECT new ru.practicum.shareit.item.dto.CommentDto(" +
            "c.id, c.text, u.name, c.created) " +
            "FROM Comment c " +
            "JOIN User u ON c.authorId = u.id " +
            "WHERE c.itemId = :itemId " +
            "ORDER BY c.id")
    List<CommentDto> findCommentDtosByItemId(@Param("itemId") Long itemId);
}