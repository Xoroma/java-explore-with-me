package ru.practicum.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Request;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findAllByEventId(long eventId);

    List<Request> findAllByIdIn(List<Long> requests);

    List<Request> findByUserId(long userId);

    Boolean existsByUserIdAndEventId(long userId, long eventId);
}
