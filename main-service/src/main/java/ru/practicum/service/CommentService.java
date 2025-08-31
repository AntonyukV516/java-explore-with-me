package ru.practicum.service;

import jakarta.transaction.Transactional;
import ru.practicum.model.dto.comment.CommentDto;
import ru.practicum.model.dto.comment.CommentUpdateDto;

import java.util.List;

public interface CommentService {

    @Transactional
    CommentDto addComment(Long userId, Long eventId, CommentDto commentNewDto);

    @Transactional
    CommentDto updateComment(Long userId, CommentUpdateDto commentUpdateDto);

    @Transactional
    void deletePrivateComment(Long userId, Long commentId);

    @Transactional
    List<CommentDto> getCommentsByUserId(String rangeStart, String rangeEnd, Long userId, Integer from, Integer size);

    @Transactional
    List<CommentDto> getComments(String rangeStart, String rangeEnd, Integer from, Integer size);

    @Transactional
    void deleteAdminComment(Long commentId);

    @Transactional
    List<CommentDto> getCommentsByEventId(String rangeStart, String rangeEnd, Long eventId, Integer from, Integer size);
}
