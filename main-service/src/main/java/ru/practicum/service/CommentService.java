package ru.practicum.service;

import ru.practicum.model.dto.comment.CommentDto;
import ru.practicum.model.dto.comment.CommentUpdateDto;

import java.util.List;

public interface CommentService {

    CommentDto addComment(Long userId, Long eventId, CommentDto commentNewDto);

    CommentDto updateComment(Long userId, CommentUpdateDto commentUpdateDto);

    void deletePrivateComment(Long userId, Long commentId);

    List<CommentDto> getCommentsByUserId(String rangeStart, String rangeEnd, Long userId, Integer from, Integer size);

    List<CommentDto> getComments(String rangeStart, String rangeEnd, Integer from, Integer size);

    void deleteAdminComment(Long commentId);

    List<CommentDto> getCommentsByEventId(String rangeStart, String rangeEnd, Long eventId, Integer from, Integer size);
}
