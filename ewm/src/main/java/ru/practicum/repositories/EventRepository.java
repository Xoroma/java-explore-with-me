package ru.practicum.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Event;
import ru.practicum.stats.State;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findByInitiatorId(long userId, Pageable pageable);

    @Query(value = "select e " +
            " from Event e " +
            " where (:ids is null or e.initiator.id in :ids) " +
            " AND (:state is null or e.state in :state)" +
            " and (:cat is null or e.category.id in :cat)" +
            " AND (e.eventDate between coalesce(:start, e.eventDate) and coalesce(:end, e.eventDate))")
    Page<Event> searchEvents(@Param("ids") Collection<Long> initiator,
                             @Param("state") Collection<State> state,
                             @Param("cat") Collection<Long> category,
                             @Param("start") LocalDateTime eventDate,
                             @Param("end") LocalDateTime eventDate2,
                             Pageable pageable);

    @Query(value =
            "select e " +
                    " from Event e " +
                    " where (:text is null" +
                    " or lower(e.annotation) like concat('%', lower(:text), '%')" +
                    " or lower(e.description) like concat('%', lower(:text), '%'))" +
                    " and (:cat is null or e.category.id in :cat)" +
                    " and (:paid is null or e.paid = :paid)" +
                    " and (e.eventDate between coalesce(:start, e.eventDate) and coalesce(:end, e.eventDate)" +
                    " and :available is null or e.participantLimit = 0 or (SELECT COUNT(r) from Request r WHERE r.event.id = e.id AND r.status = 'CONFIRMED') < e.participantLimit)")
    List<Event> findEventsByFilters(@Param("text") String text,
                                    @Param("cat") List<Long> categories,
                                    @Param("paid") Boolean paid,
                                    @Param("start") LocalDateTime rangeStart,
                                    @Param("end") LocalDateTime rangeEnd,
                                    @Param("available") Boolean onlyAvailable);

    Boolean existsByCategoryId(long catId);
}
