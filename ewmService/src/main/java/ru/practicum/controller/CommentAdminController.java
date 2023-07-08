package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.service.comment.CommentService;

import javax.validation.constraints.PositiveOrZero;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/comments")
@Validated
public class CommentAdminController {
    private final CommentService commentService;

    @DeleteMapping("/{comId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByAdmin(@PathVariable @PositiveOrZero Long comId) {
        log.info("Удаление комментария с ID = {} администратором. " +
                "DELETE /admin/comments/{}", comId, comId);
        commentService.deleteCommentByAdmin(comId);
    }
}
