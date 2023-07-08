package ru.practicum.service.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.comment.CommentForView;
import ru.practicum.dto.comment.CommentUserDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.FoundConflictInDB;
import ru.practicum.exception.NotFoundRecordInBD;
import ru.practicum.exception.OperationFailedException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.EventState;
import ru.practicum.model.User;
import ru.practicum.repository.CommentRepository;
import ru.practicum.service.event.EventService;
import ru.practicum.service.user.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final EventService eventService;
    private final UserService userService;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentForView addComment(Long userId, CommentUserDto inputCommentDto) {
        User userFromDb = userService.getUserOrThrow(userId, "При создании комментария пользователем " +
                "в базе данных не найден пользователь с ID = %d.");
        Event eventFromDb = eventService.getEventOrThrow(inputCommentDto.getEventId(),
                "При создании комментария пользователем в базе данных не найдено событие с ID = %d.");

        if (!eventFromDb.getEventState().equals(EventState.PUBLISHED)) {
            if (eventFromDb.getInitiator().getId().equals(userId)) {
                Comment comment = commentMapper.mapToModelFromDto(inputCommentDto, eventFromDb, userFromDb);
                comment.setCreatedOn(LocalDateTime.now());
                Comment result = commentRepository.save(comment);
                log.info("Инициатором события создан комментарий с ID = {} в БД.", result.getId());
                return commentMapper.mapToView(result);
            } else {
                log.info("Пользователь пытается сделать комментарий неопубликованного события.");
                throw new OperationFailedException("Пользователь пытается сделать комментарий " +
                        "неопубликованного события.");
            }
        }

        Comment comment = commentMapper.mapToModelFromDto(inputCommentDto, eventFromDb, userFromDb);
        comment.setCreatedOn(LocalDateTime.now());
        Comment result = commentRepository.save(comment);
        log.info("Обычные пользователем создан комментарий с ID = {} в БД.", result.getId());
        return commentMapper.mapToView(result);
    }

    @Override
    public CommentForView getCommentById(Long userId, Long comId) {
        User userFromDb = userService.getUserOrThrow(userId, "При получении комментария пользователем " +
                "в базе данных не найден пользователь с ID = %d.");
        Comment commentFromDb = getCommentOrThrow(comId, "При получении комментария пользователем " +
                "по ID = %d этот комментарий не найден в БД.");
        Event eventFromDb = eventService.getEventOrThrow(commentFromDb.getEvent().getId(),
                "Не найдено событие %d при получении комментария к нему.");

        if (!eventFromDb.getEventState().equals(EventState.PUBLISHED)) {

            if (eventFromDb.getInitiator().getId().equals(userId)) {

                CommentForView result = commentMapper.mapToView(commentFromDb);
                log.info("Отправлен результат запроса комментария по ID = {}", comId);
                return result;
            } else {

                log.info("Пользователь пытается получить комментарий неопубликованного события.");
                throw new OperationFailedException("Пользователь пытается сделать комментарий " +
                        "неопубликованного события.");
            }
        }

        CommentForView result = commentMapper.mapToView(commentFromDb);
        log.info("Отправлен результат запроса комментария по ID = {}", comId);
        return result;
    }

    @Override
    @Transactional
    public void deleteCommentByUser(Long comId, Long userId) {
        UserDto userFromDb = userService.check(userId, "При удалении комментария пользователем " +
                "в базе данных не найден пользователь с ID = %d.");
        Comment commentFromDb = getCommentOrThrow(comId, "При удалении комментария пользователем " +
                "комментарий с ID = %d не найден в БД.");
        if (checkAuthorComment(commentFromDb, userId)) {
            commentRepository.deleteById(comId);
            log.info("Из БД пользователем с ID = {} удалён комментарий с ID = {}", userId, comId);
            return;
        }
        throw new OperationFailedException(String.format("Ошибка удаления комментария. Пользователь с ID = %d " +
                        "не является автором комментария с ID = %d. Его автор с ID = %d",
                userId, comId, commentFromDb.getUser().getId()));
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(Long comId) {
        commentRepository.findById(comId).orElseThrow(() -> new NotFoundRecordInBD(String.format(
                "При удалении комментария с ID = %d администратором комментарий не найден в БД.", comId)));
        System.out.println("Найден комментарий перед удалением.");
        commentRepository.deleteById(comId);
        log.info("Администратором выполнено удаление комментария с ID = {}.", comId);
    }

    @Override
    @Transactional
    public CommentForView updateComment(Long comId, Long userId, CommentUserDto inputCommentDto) {
        UserDto userFromDb = userService.check(userId, "При получении комментария пользователем " +
                "в базе данных не найден пользователь с ID = %d.");
        Comment commentFromDb = getCommentOrThrow(comId, "При получении комментария пользователем " +
                "комментарий с ID = %d не найден в БД.");

        checkAuthorCommentOrThrow(userId, commentFromDb);


        if (commentFromDb.getCreatedOn().isBefore(LocalDateTime.now().minusHours(2))) {

            throw new OperationFailedException("Прошло более 2 часов после создания " +
                    "комментария, редактировать его уже нельзя.");
        }
        Comment comment = updateFieldsByUser(inputCommentDto, commentFromDb);
        Comment result = commentRepository.save(comment);

        log.info("Выполнено обновление комментария с ID = {}.", comId);
        return commentMapper.mapToView(result);
    }

    @Override
    public List<CommentForView> getCommentsForEvent(Long eventId, int from, int size) {
        Pageable pageable = PageRequest.of(
                from == 0 ? 0 : (from / size), size);
        Event eventFromDb = eventService.getEventOrThrow(eventId, "При получении списка комментариев " +
                "к событию не найдено событиес ID = % в БД.");
        if (!eventFromDb.getEventState().equals(EventState.PUBLISHED)) {
            if (eventFromDb.getInitiator().getId().equals(eventId)) {
                List<Comment> comments = commentRepository.findAllByEvent_Id(eventId, pageable);
                List<CommentForView> result = commentMapper.mapFromModelLisToViewList(comments);
                log.info("Инициатору выдан список комментариев к событию с ID = {}, состоящий из {} комментариев.",
                        eventId, result.size());
                return result;
            } else {
                log.info("Выдан пустой список комментариев к событию с ID = {}, поскольку событие не опубликовано.",
                        eventId);
                return Collections.emptyList();
            }
        }
        List<Comment> comments = commentRepository.findAllByEvent_Id(eventId, pageable);
        List<CommentForView> result = commentMapper.mapFromModelLisToViewList(comments);
        log.info("Обычному полбзователю выдан список комментариев к событию с ID = {}, " +
                "состоящий из {} комментариев.", eventId, result.size());
        return result;
    }

    @Override
    public Comment getCommentOrThrow(Long comId, String message) {
        if (message == null || message.isBlank()) {
            message = "В БД не найден комментарий с ID = %d.";
        }
        String finalMessage = message;
        return commentRepository.findById(comId).orElseThrow(
                () -> new NotFoundRecordInBD(String.format(finalMessage, comId)));
    }

    private Comment updateFieldsByUser(CommentUserDto newComment, Comment oldComment) {
        String text = newComment.getText();
        return oldComment.toBuilder()
                .text(text)
                .editedOn(LocalDateTime.now())
                .isEdited(true).build();
    }

    private boolean checkAuthorComment(Comment comment, Long userId) {
        return comment.getUser().getId().equals(userId);
    }

    private void checkAuthorCommentOrThrow(Long userId, Comment comment) {
        if (!comment.getUser().getId().equals(userId)) {
            throw new FoundConflictInDB(String.format("Пользователь с ID = %d не является автором" +
                            " комментария с ID = %d. Настоящий автор с ID = %d",
                    comment.getId(), userId, comment.getUser().getId()));
        }
    }

}
