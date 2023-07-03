package ru.practicum.service.event;

import ru.practicum.dto.event.*;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.model.Event;
import ru.practicum.model.EventState;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {

    List<EventShortDto> getMyEvents(Long userId, Integer from, Integer size);

    EventFullDto create(Long userId, NewEventDto newEventDto);

    List<EventFullDto> getEventsForAdmin(List<Long> usersId, List<EventState> states,
                                         List<Long> categoriesId, LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd, String text, Integer from, Integer size);

    List<EventShortDto> getEventsForAll(String text, List<Long> categoriesIds, Boolean paid,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                        Boolean onlyAvailable, String sort,
                                        Integer from, Integer size,
                                        HttpServletRequest httpServletRequest);

    EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);

    Event getEventOrThrow(Long eventId, String message);

    EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest);

    EventFullDto getEventById(Long eventId, HttpServletRequest httpServletRequest);

    EventFullDto getMyEventById(Long eventId, Long userId);

    EventFullDto cancelEvent(Long userId, Long eventId);

    EventFullDto updateEventUser(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    List<ParticipationRequestDto> getRequestsEvent(Long userId, Long eventId);

    EventFullDto publish(Long eventId);
}
