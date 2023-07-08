package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.dto.comment.CommentEvent;
import ru.practicum.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("select count(c) from Comment c where c.event.id = ?1")
    Long countCommentsForEvent(Long id);

    List<Comment> findAllByEvent_Id(Long id, Pageable pageable);

    @Query("select new ru.practicum.dto.comment.CommentEvent(c.event.id, count(c))" +
            "from Comment c where c.event.id in ?1 group by c.event.id")
    List<CommentEvent> getCommentsEvents(List<Long> eventIds);


}
