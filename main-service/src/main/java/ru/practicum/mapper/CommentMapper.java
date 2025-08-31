package ru.practicum.mapper;


import ru.practicum.dto.SimpleDateTimeFormatter;
import ru.practicum.model.dto.comment.CommentDto;
import ru.practicum.model.entity.Comment;
import ru.practicum.model.entity.Event;
import ru.practicum.model.entity.User;

import java.util.ArrayList;
import java.util.List;

public class CommentMapper {

    public static Comment toComment(CommentDto commentNewDto, User user, Event event) {
        Comment comment = Comment.builder()
                .user(user)
                .event(event)
                .message(commentNewDto.getMessage())
                .created(SimpleDateTimeFormatter.CURRENT_TIME)
                .build();
        return comment;
    }

    public static CommentDto toCommentDto(Comment comment) {
        CommentDto commentFullDto = CommentDto.builder()
                .id(comment.getId())
                .user(UserMapper.toUserDto(comment.getUser()))
                .event(EventMapper.toEventDto(comment.getEvent()))
                .message(comment.getMessage())
                .created(comment.getCreated())
                .build();
        return commentFullDto;
    }

    public static List<CommentDto> toCommentDtoList(Iterable<Comment> comments) {
        List<CommentDto> result = new ArrayList<>();

        for (Comment comment : comments) {
            result.add(toCommentDto(comment));
        }
        return result;
    }
}
