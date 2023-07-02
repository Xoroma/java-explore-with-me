package ru.practicum.services.publics;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.Constants;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.explore.stats.HitDto;
import ru.practicum.explore.stats.StatsClient;
import ru.practicum.explore.stats.VisitDto;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Event;
import ru.practicum.repositories.EventRepository;
import ru.practicum.stats.Sorted;
import ru.practicum.stats.State;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class PublicEventService {
    private EventRepository eventRepository;
    private StatsClient statsClient;

    public List<EventShortDto> getEventByFilters(String text,
                                                 List<Long> categories,
                                                 Boolean paid,
                                                 LocalDateTime rangeStart,
                                                 LocalDateTime rangeEnd,
                                                 Boolean onlyAvailable,
                                                 String sort,
                                                 int from,
                                                 int size,
                                                 String ip,
                                                 String endpoint) {
        List<Event> events;
        if (rangeStart == null || rangeStart.isBefore(rangeEnd)) {
            events = eventRepository.findEventsByFilters(text, categories, paid, rangeStart, rangeEnd, onlyAvailable);
        } else {
            throw new ValidationException("Start date cannot be after end date");
        }

        if (events.isEmpty()) {
            return List.of();
        }

        String minPublishedDate = events.stream().map(Event::getEventDate)
                .min(Comparator.naturalOrder())
                .orElse(LocalDateTime.parse(Constants.START, DateTimeFormatter.ofPattern(Constants.DATE_FORMAT))).format(Constants.FORMATTER);

        List<EventShortDto> eventDtos = EventMapper.toEventShortDtoList(events);

        List<String> uris = events.stream()
                .map(event -> "event/" + event.getId())
                .collect(Collectors.toList());

        List<VisitDto> visits = visits(minPublishedDate, uris);
        eventDtos.stream()
                .flatMap(event -> visits.stream()
                        .filter(visit -> visit.getUri().endsWith("/" + event.getId()))
                        .map(visit -> {
                            event.setViews(visit.getHits());
                            return event;
                        }))
                .distinct()
                .collect(Collectors.toList());

        if (sort != null) {
            if (Sorted.EVENT_DATE.toString().equals(sort)) {
                eventDtos.sort(Comparator.comparing(EventShortDto::getEventDate).reversed());
            } else if (Sorted.VIEWS.toString().equals(sort)) {
                eventDtos.sort(Comparator.comparing(EventShortDto::getViews).reversed());
            }
        } else {
            eventDtos.sort(Comparator.comparing(EventShortDto::getId).reversed());
        }

        List<EventShortDto> pagedEvents = eventDtos
                .stream()
                .skip((long) from * (long) size)
                .limit(size)
                .collect(Collectors.toList());

        Page<EventShortDto> page = new PageImpl<>(pagedEvents, PageRequest.of(from, size), eventDtos.size());

        addHit(new HitDto(Constants.APP_NAME, endpoint, ip, LocalDateTime.now()));
        return page.getContent();
    }

    public EventFullDto getEventById(long id,
                                     String ip,
                                     String endpoint) {
        EventFullDto event = EventMapper.toEventDto(eventRepository.findById(id).orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found")));

        if (event.getState().equals(State.PUBLISHED)) {
            for (VisitDto vd : visits(event.getPublishedOn().format(Constants.FORMATTER), List.of(endpoint))) {
                if (vd.getUri().endsWith("/" + event.getId())) {
                    event.setViews(vd.getHits());
                }
            }

            addHit(new HitDto(Constants.APP_NAME, endpoint, ip, LocalDateTime.now()));
            return event;
        }
        throw new NotFoundException("Event with id=" + id + " was not found");
    }

    private void addHit(HitDto hitDto) {
        statsClient.addHit(hitDto);
    }

    private List<VisitDto> visits(String start, List<String> uris) {
        return statsClient.getStats(start, Constants.END, uris, true);
    }
}
