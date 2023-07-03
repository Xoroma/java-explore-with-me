package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.model.ParticipationRequest;

import java.util.List;

@Repository
public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    @Query("select p from ParticipationRequest p where p.requester.id = ?1 order by p.id")
    List<ParticipationRequest> findAllByRequesterIdOrderByIdAsc(Long userId);

    @Query("select count(p) from ParticipationRequest p where p.requester.id = ?1 and p.event.id = ?2")
    int countAllByRequester_IdAndEvent_Id(Long userId, Long eventId);


    @Query("select p from ParticipationRequest p where p.event.id = ?1 and p.statusRequest = 'CONFIRMED'")
    List<ParticipationRequest> findConfirmedRequests(Long eventId);


    @Query("select p from ParticipationRequest p where p.statusRequest = 'CONFIRMED' and p.event.id in ?1")
    List<ParticipationRequest> findConfirmedRequests(List<Long> ids);


    List<ParticipationRequest> findByIdInOrderByIdAsc(List<Long> requestIds);

    List<ParticipationRequest> findAllByEvent_Id(Long eventId);
}
