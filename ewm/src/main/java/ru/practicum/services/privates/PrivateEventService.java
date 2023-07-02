package ru.practicum.services.privates;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.*;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.Event;
import ru.practicum.model.Request;
import ru.practicum.repositories.EventRepository;
import ru.practicum.repositories.RequestRepository;
import ru.practicum.services.admins.AdminUserService;
import ru.practicum.services.publics.PublicCategoryService;
import ru.practicum.stats.State;
import ru.practicum.stats.Status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.practicum.valid.UpdateEventValid.valid;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class PrivateEventService {
    private PublicCategoryService categoryService;
    private AdminUserService adminService;
    private EventRepository eventRepository;
    private RequestRepository requestRepository;

    @Transactional
    public EventFullDto createEvent(long userId, NewEventDto eventDto) {
        if (LocalDateTime.now().plusHours(2).isBefore(eventDto.getEventDate())) {
            Event event = EventMapper.toEvent(eventDto);
            event.setCategory(CategoryMapper.toCategory(categoryService.getCategory(eventDto.getCategory())));
            event.setCreatedOn(LocalDateTime.now());
            event.setInitiator(UserMapper.toUser(adminService.getUser(userId)));
            event.setState(State.PENDING);
            return EventMapper.toEventDto(eventRepository.save(event));
        }
        throw new ValidationException("Conflict of parameters date and time");
    }

    public List<EventShortDto> getEvents(long userId, int from, int size) {
        List<EventShortDto> eventShortDtos = EventMapper.toEventShortDtoList(eventRepository.findByInitiatorId(userId, PageRequest.of(from / size, size)).toList());

        if (eventShortDtos.isEmpty()) {
            return List.of();
        }

        return eventShortDtos;
    }

    public EventFullDto getFullEvent(long userId, long eventId) {
        EventFullDto event = EventMapper.toEventDto(eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found")));

        if (event.getInitiator().getId() == userId) {
            return event;
        } else {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }
    }

    @Transactional
    public EventFullDto updateEvent(long userId, long eventId, UpdateEventUserRequest eventRequest) {
        LocalDateTime timeValid = LocalDateTime.now().plusHours(2);
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        String state = Optional.ofNullable(eventRequest.getStateAction())
                .map(cat -> eventRequest.getStateAction().name())
                .orElse(null);

        if (timeValid.isBefore(event.getEventDate()) &&
                !event.getState().equals(State.PUBLISHED)) {
            if (event.getInitiator().getId() == userId) {
                if (eventRequest.getCategory() != null) {
                    event.setCategory(CategoryMapper.toCategory(categoryService.getCategory(eventRequest.getCategory())));
                }
                return valid(event, eventRequest, state);
            } else {
                throw new ConflictException("You do not have rights to edit this event");
            }
        } else {
            throw new ConflictException("Conflict of parameters date or state");
        }
    }

    @Transactional
    public List<ParticipationRequestDto> getRequestEvent(long userId, long eventId) {
        List<ParticipationRequestDto> dtos = RequestMapper.toRequestDtoList(requestRepository.findAllByEventId(eventId));
        adminService.getUser(userId);

        if (dtos.isEmpty()) {
            return List.of();
        }

        return dtos;
    }

    @Transactional
    public EventRequestStatusUpdateResult changeEventStatus(EventRequestStatusUpdateRequest eventRequest, long userId, long eventId) {
        EventRequestStatusUpdateResult eventResult = new EventRequestStatusUpdateResult();
        List<Request> requests = requestRepository.findAllByIdIn(eventRequest.getRequestIds());
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));


        if (!event.isRequestModeration() || event.getParticipantLimit() == 0 && event.getInitiator().getId() == userId) {
            return new EventRequestStatusUpdateResult();
        }

        for (Request prd : requests) {
            if (prd.getStatus().equals(Status.PENDING) && event.getConfirmedRequests() < event.getParticipantLimit()) {
                if (eventRequest.getStatus().equals(Status.CONFIRMED)) {
                    prd.setStatus(Status.CONFIRMED);
                    if (eventResult.getConfirmedRequests() == null) {
                        eventResult.setConfirmedRequests(new ArrayList<>());
                    }
                    eventResult.getConfirmedRequests().add(RequestMapper.toRequestDto(prd));
                    event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                } else {
                    prd.setStatus(Status.REJECTED);

                    if (eventResult.getRejectedRequests() == null) {
                        eventResult.setRejectedRequests(new ArrayList<>());
                    }

                    eventResult.getRejectedRequests().add(RequestMapper.toRequestDto(prd));
                }
            } else {
                throw new ConflictException("The participant limit has been reached");
            }
        }
        return eventResult;
    }
}
