package ru.practicum.controllers.privates;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.services.privates.PrivateRequestService;

import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
@Slf4j
@Validated
public class PrivateRequestController {
    private PrivateRequestService privateRequestService;

    @GetMapping("/{userId}/requests")
    public List<ParticipationRequestDto> getRequests(@Positive @PathVariable long userId) {
        log.info("Получение информации о заявках текущего пользователя id: {}", userId);
        return privateRequestService.getRequests(userId);
    }

    @PostMapping("/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@Positive @PathVariable long userId,
                                                 @Positive @RequestParam long eventId) {
        log.info("Добавление запроса на участие пользователем id: {}", userId);
        return privateRequestService.createRequest(userId, eventId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@Positive @PathVariable long userId,
                                                 @Positive @PathVariable long requestId) {
        log.info("Отмена запроса на участие в событии id: {}", requestId);
        return privateRequestService.cancelRequest(userId, requestId);
    }
}
