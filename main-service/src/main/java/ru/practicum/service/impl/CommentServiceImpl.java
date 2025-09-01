package ru.practicum.service.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.SimpleDateTimeFormatter;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.DateValidationException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.model.dto.comment.CommentDto;
import ru.practicum.model.dto.comment.CommentUpdateDto;
import ru.practicum.model.entity.Comment;
import ru.practicum.model.entity.Event;
import ru.practicum.model.entity.User;
import ru.practicum.repository.CommentRepository;
import ru.practicum.service.CommentService;
import ru.practicum.service.EventService;
import ru.practicum.service.UserService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.practicum.dto.SimpleDateTimeFormatter.CURRENT_TIME;

@Service
@AllArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final EventService eventService;

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long eventId, CommentDto commentNewDto) {
        User user = userService.findUserById(userId);
        Event event = eventService.findEventById(eventId);
        return CommentMapper.toCommentDto(commentRepository.save(CommentMapper.toComment(commentNewDto, user, event)));
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, CommentUpdateDto commentUpdateDto) {
        Comment comment = findCommentById(commentUpdateDto.getId());
        verifyCommentOwnership(comment, userId);
        comment.setMessage(commentUpdateDto.getMessage());
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deletePrivateComment(Long userId, Long commentId) {
        userService.findUserById(userId);
        verifyCommentOwnership(findCommentById(commentId), userId);
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional
    public List<CommentDto> getCommentsByUserId(String rangeStart, String rangeEnd, Long userId, Integer from, Integer size) {
        userService.findById(userId);
        Map<String, LocalDateTime> dateRange = getDateRange(rangeStart, rangeEnd);
        List<Comment> comments = commentRepository.getCommentsByUserId(userId, dateRange.get("startTime"),
                dateRange.get("endTime"), getPageable(from, size));
        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .toList();
    }

    @Override
    @Transactional
    public List<CommentDto> getComments(String rangeStart, String rangeEnd, Integer from, Integer size) {
        Map<String, LocalDateTime> dateRange = getDateRange(rangeStart, rangeEnd);
        List<Comment> commentList = commentRepository.getComments(dateRange.get("startTime"),
                dateRange.get("endTime"), getPageable(from, size));
        return CommentMapper.toCommentDtoList(commentList);
    }

    @Override
    @Transactional
    public void deleteAdminComment(Long commentId) {
        findCommentById(commentId);
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional
    public List<CommentDto> getCommentsByEventId(String rangeStart, String rangeEnd, Long eventId, Integer from, Integer size) {
        eventService.findEventById(eventId);
        Map<String, LocalDateTime> dateRange = getDateRange(rangeStart, rangeEnd);
        List<Comment> commentList = commentRepository.getCommentsByEventId(eventId, dateRange.get("startTime"),
                dateRange.get("endTime"), getPageable(from, size));
        return CommentMapper.toCommentDtoList(commentList);
    }

    private CommentDto findById(Long commentId) {
        return CommentMapper.toCommentDto(findCommentById(commentId));
    }

    private Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(
                () -> new NotFoundException("Комментария с id " + commentId + " не существует."));
    }

    private void verifyCommentOwnership(Comment comment, Long userId) {
        if (!userId.equals(comment.getUser().getId())) {
            throw new ConditionsNotMetException("Пользователь с id " + userId +
                    " не является автором комментария с id " + comment.getId() + ".");
        }
    }

    private Map<String, LocalDateTime> getDateRange(String startTime, String endTime) {
        Map<String, LocalDateTime> dateRange = new HashMap<>();
        dateRange.put("startTime", parseDate(startTime));
        dateRange.put("endTime", parseDate(endTime));

        if (startTime != null && endTime != null) {
            if (dateRange.get("startTime").isAfter(dateRange.get("endTime"))) {
                throw new DateValidationException("Дата начала должна быть после End.");
            }
            if (dateRange.get("endTime").isAfter(CURRENT_TIME) || dateRange.get("startTime").isAfter(CURRENT_TIME)) {
                throw new DateValidationException("Дата конца должна должна быть в прошлом.");
            }
        }

        return dateRange;
    }

    private LocalDateTime parseDate(String date) {
        return date != null ? SimpleDateTimeFormatter.parse(date) : null;
    }

    private Pageable getPageable(Integer from, Integer size) {
        Pageable pageable = Pageable.unpaged();

        if (from != null && size != null) {
            pageable = Pageable.ofSize(size).withPage(from / size);
        }

        return pageable;
    }
}