package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.service.event.EventService;
import ru.practicum.validation.CreateObject;
import ru.practicum.validation.UpdateObject;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@Slf4j
@RequiredArgsConstructor
@Validated
public class EventPrivateController {
    private final EventService eventService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventShortDto> getMyEvents(@PathVariable @Positive Long userId,
                                           @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                           @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получение событий, добавленных текущим пользователем с ID = {}.", userId);
        return eventService.getMyEvents(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@PathVariable("userId") @Positive Long userId,
                                 @Validated(CreateObject.class) @RequestBody NewEventDto newEventDto) {

        log.info("Добавление нового события. POST /users/userId/events userId={}, newEvent = {}.", userId, newEventDto);
        return eventService.create(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto getEventById(@PathVariable("userId") @Positive Long userId,
                                     @PathVariable("eventId") @Positive Long eventId) {
        log.info("Получение полной информации о событии, добавленном текущим пользователем.\n" +
                "GET /users/{userId}/events/{eventId}: userId = {}, eventId = {}", userId, eventId);

        return eventService.getMyEventById(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEvent(@PathVariable @Positive Long userId,
                                    @PathVariable @Positive Long eventId,
                                    @Validated(UpdateObject.class) @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        log.info("Обновление события от пользователя. Patch /users/{}/events/ updateEvent = {}.",
                userId, updateEventUserRequest);
        return eventService.updateEventUser(userId, eventId, updateEventUserRequest);
    }

    @GetMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getRequestsOld(@PathVariable @Positive Long userId,
                                                        @PathVariable @Positive Long eventId) {
        log.info("GET /users/{userId}/events//{eventId}/requests userId {}, eventId {}", userId, eventId);
        return eventService.getRequestsEvent(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    @ResponseStatus(HttpStatus.OK)
    public EventRequestStatusUpdateResult updateRequestsStatus(@Positive @PathVariable Long userId,
                                                               @Positive @PathVariable Long eventId,
                                                               @RequestBody EventRequestStatusUpdateRequest
                                                                       eventRequestStatusUpdateRequest) {

        log.info("Изменение статуса (подтверждена, отменена) заявок на участие в событии текущего пользователя.\n" +
                "PATCH /users/{userId}/events//{eventId}/requests userId = {}, eventId = {}," +
                " Тело запроса: = {}", userId, eventId, eventRequestStatusUpdateRequest);

        return eventService.updateRequestStatus(userId, eventId, eventRequestStatusUpdateRequest);
    }

}
