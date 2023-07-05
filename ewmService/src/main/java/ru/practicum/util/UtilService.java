package ru.practicum.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.WebClientService;
import ru.practicum.dto.StatsDtoForView;
import ru.practicum.exception.StatsException;
import ru.practicum.model.Event;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.repository.ParticipationRequestRepository;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Import({WebClientService.class})
public class UtilService {
    private final ParticipationRequestRepository requestRepository;
    private final WebClientService statsClient;

    public List<StatsDtoForView> getViews(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> uris = new ArrayList<>();
        LocalDateTime start = null;
        for (Event event : events) {
            uris.add("/events/" + event.getId());
            if (start == null) {
                start = event.getCreatedOn();
            } else if (start.isBefore(event.getCreatedOn())) {
                start = event.getCreatedOn();
            }
        }
        List<StatsDtoForView> stats = new ArrayList<>();
        try {
            log.info("Запрашиваем статистику от сервера статистики.");
            stats = statsClient.getStats(start, LocalDateTime.now(), uris, true);
        } catch (StatsException e) {
            log.error(String.format("Ошибка сервиса статистики при получении информации об %s:\t\t%s", uris,
                    e.getMessage()));
        }
        return stats;
    }

    public List<Event> fillViews(List<Event> events, List<StatsDtoForView> stats) {
        List<Event> result = new ArrayList<>();
        if (stats != null && !stats.isEmpty()) {
            for (Event ev : events) {

                for (StatsDtoForView statsDtoForView : stats) {
                    String[] statsFields = statsDtoForView.getUri().split("/");
                    if (Integer.parseInt(statsFields[2]) == ev.getId()) {
                        ev.setViews(statsDtoForView.getHits());
                        result.add(ev);
                    }
                }
            }
        } else {
            for (Event event : events) {
                event.setViews(0);
                result.add(event);
            }
        }

        return result;
    }

    public Map<Event, List<ParticipationRequest>> prepareConfirmedRequest(List<Event> events) {

        log.info("Получаем список подтверждённых запросов для всех событий.");
        List<Long> list1 = new ArrayList<>();
        for (Event event1 : events) {
            list1.add(event1.getId());
        }
        List<ParticipationRequest> confirmedRequests = requestRepository.findConfirmedRequests(list1);

        Map<Event, List<ParticipationRequest>> result = new HashMap<>();

        for (ParticipationRequest request : confirmedRequests) {
            Event event = request.getEvent();
            List<ParticipationRequest> list = result.get(event);
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(request);
            result.put(event, list);
        }
        return result;
    }


    public List<Event> fillConfirmedRequests(List<Event> events, Map<Event, List<ParticipationRequest>> confirmedRequests) {
        if (confirmedRequests == null || confirmedRequests.isEmpty()) {
            log.info("Список событий пуст или равен null. Вот он: {}.", confirmedRequests);
            for (Event event : events) {
                event.setConfirmedRequests(0);
            }
            return events;
        }

        for (Event event : events) {
            if (confirmedRequests.get(event).isEmpty()) {
                event.setConfirmedRequests(0);
            } else {
                event.setConfirmedRequests(confirmedRequests.get(event).size());
            }
        }
        return events;
    }
}
