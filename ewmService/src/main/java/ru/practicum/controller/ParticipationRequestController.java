package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.service.request.ParticipationRequestService;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/requests")
@Validated
public class ParticipationRequestController {
    private final ParticipationRequestService participationRequestService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ParticipationRequestDto> getRequests(@PathVariable @Positive Long userId) {
        log.info("Получение информации о заявках текущего пользователя на участие в чужих событиях." +
                "GET /users/{}/requests userId", userId);
        return participationRequestService.getRequestsByUserId(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto addRequest(@PathVariable @Positive Long userId,
                                              @RequestParam @NotNull Long eventId) {
        log.info("Добавление запроса от текущего пользователя на участие в событии.\t\tPOST  /users/{userId}/requess userId {}, eventId {}", userId, eventId);
        return participationRequestService.create(userId, eventId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable("userId") @Positive Long userId,
                                                 @PathVariable(name = "requestId") @Positive Long requestId) {
        log.info(String.format("Отмена своего запроса с ID = %d на участие в событии пользователя с ID = %d.\n" +
                "PATCH /users/{userId}/requests/{requestId}/cancel", requestId, userId));
        return participationRequestService.cancelRequest(userId, requestId);
    }

}
