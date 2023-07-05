package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.model.EventState;
import ru.practicum.service.event.EventService;
import ru.practicum.validation.UpdateObject;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/events")
@Slf4j
@RequiredArgsConstructor
@Validated
public class EventAdminController {
    private final EventService eventService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EventFullDto> getEventsForAdmin(@RequestParam(required = false) List<Long> users,
                                                @RequestParam(required = false) List<EventState> states,
                                                @RequestParam(required = false) List<Long> categories,
                                                @RequestParam(required = false) LocalDateTime rangeStart,
                                                @RequestParam(required = false) LocalDateTime rangeEnd,
                                                @RequestParam(required = false, defaultValue = "") String text,
                                                @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                @Positive @RequestParam(defaultValue = "10") Integer size) {
        log.info("GET /admin/events users={},states={},categories={},\nrangeStart={},rangeEnd={}" +
                ",from={},size={}", users, states, categories, rangeStart, rangeEnd, from, size);

        return eventService.getEventsForAdmin(users, states, categories,
                rangeStart, rangeEnd, text, from, size);
    }

    @PatchMapping("/{eventId}")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto update(@PositiveOrZero @PathVariable Long eventId,
                               @Validated(UpdateObject.class) @RequestBody UpdateEventAdminRequest updateEventAdminRequest) {
        log.info("Обновление события администратором.\t\tPATCH /admin/events event id{}, {}", eventId,
                updateEventAdminRequest);
        return eventService.updateEventAdmin(eventId, updateEventAdminRequest);
    }

    @PatchMapping("/{eventId}/publish")
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto publishEvent(@Positive @PathVariable Long eventId) {
        log.info("Публикация события с ID = {}", eventId);
        return eventService.publish(eventId);
    }
}