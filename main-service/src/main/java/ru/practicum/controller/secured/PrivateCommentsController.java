package ru.practicum.controller.secured;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.model.dto.comment.CommentDto;
import ru.practicum.model.dto.comment.CommentUpdateDto;
import ru.practicum.service.CommentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/users/comments")
@Validated
public class PrivateCommentsController {
    private final CommentService commentService;

    @PostMapping("/{userId}/{eventId}")
    @ResponseStatus(value = HttpStatus.CREATED)
    public CommentDto addComment(@Valid @RequestBody CommentDto commentNewDto,
                                 @PathVariable Long userId,
                                 @PathVariable Long eventId) {

        return commentService.addComment(userId, eventId, commentNewDto);
    }

    @PatchMapping("/{userId}")
    @ResponseStatus(value = HttpStatus.OK)
    public CommentDto updateComment(@Valid @RequestBody CommentUpdateDto commentUpdateDto,
                                    @PathVariable Long userId) {

        return commentService.updateComment(userId, commentUpdateDto);
    }

    @DeleteMapping("/{userId}/{commentId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId,
                              @PathVariable Long commentId) {

        commentService.deletePrivateComment(userId, commentId);
    }

    @GetMapping("/{userId}")
    @ResponseStatus(value = HttpStatus.OK)
    public List<CommentDto> getCommentsByUserId(@PathVariable Long userId,
                                                @RequestParam(required = false, name = "rangeStart") String rangeStart,
                                                @RequestParam(required = false, name = "rangeEnd") String rangeEnd,
                                                @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {

        return commentService.getCommentsByUserId(rangeStart, rangeEnd, userId, from, size);
    }
}
