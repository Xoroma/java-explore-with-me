package ru.practicum.service.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.exception.NotFoundRecordInBD;
import ru.practicum.exception.OperationFailedException;
import ru.practicum.mapper.ParticipationRequestMapper;
import ru.practicum.model.*;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ParticipationRequestRepository;
import ru.practicum.service.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository participationRequestRepository;

    private final UserService userService;
    private final ParticipationRequestMapper requestMapper;
    private final EventRepository eventRepository;

    @Override
    public List<ParticipationRequestDto> getRequestsByUserId(Long userId) {
        User userFromDb = userService.getUserOrThrow(userId, "При получении информации о заявках на " +
                "участие в событиях не найден пользователь ID = %d.");
        List<ParticipationRequestDto> result = participationRequestRepository.findAllByRequesterIdOrderByIdAsc(userId).stream()
                .map(requestMapper::mapToDto).collect(Collectors.toList());
        log.info("Выдан ответ на запрос участия пользователя (ID = {}) в чужих событиях.", userId);
        return result;
    }

    @Override
    public ParticipationRequestDto create(Long userId, Long eventId) {
        User userFromDb = userService.getUserOrThrow(userId, "При создании заявки на участие в событии " +
                "не найден пользователь ID = %d.");
        Event eventFromDb = getEventOrThrow(eventId, "При создании заявки на участие " +
                "в событии не найдено событие ID = %d.");
        if (userId.equals(eventFromDb.getInitiator().getId())) {
            throw new OperationFailedException(String.format("Инициатор с ID = %d события не может создать " +
                    "запрос на участие в своём событии с ID = %d.", userId, eventId));
        }
        if (eventFromDb.getEventState() != EventState.PUBLISHED) {
            throw new OperationFailedException(
                    "Нельзя участвовать в неопубликованном событии."
            );
        }

        if (participationRequestRepository.countAllByRequester_IdAndEvent_Id(userId, eventId) != 0) {
            throw new OperationFailedException(
                    String.format("Нельзя добавить повторный запрос на участие в событии ID = %d.", eventId));
        }

        if (eventFromDb.getParticipantLimit() != 0) {

            List<ParticipationRequest> confirmedRequests =
                    participationRequestRepository.findConfirmedRequests(eventId);

            if (confirmedRequests.size() == eventFromDb.getParticipantLimit()) {
                throw new OperationFailedException(
                        String.format("Нельзя добавить запрос на участие в событии с ID = %d, поскольку достигнут " +
                                "лимит запросов.", eventId));
            }
        }

        ParticipationRequest participationRequest = new ParticipationRequest();
        participationRequest.setRequester(userFromDb);

        if (eventFromDb.getRequestModeration()) {
            participationRequest.setStatusRequest(StatusRequest.PENDING);
        } else {
            participationRequest.setStatusRequest(StatusRequest.CONFIRMED);
        }
        if (eventFromDb.getParticipantLimit() == 0) {
            participationRequest.setStatusRequest(StatusRequest.CONFIRMED);
        }

        participationRequest.setEvent(eventFromDb);
        participationRequest.setCreated(LocalDateTime.now());
        participationRequest = participationRequestRepository.save(participationRequest);
        log.info("Сохранена заявка на участие в событии с ID = {} пользователя с ID = {}.", eventId, userId);
        return requestMapper.mapToDto(participationRequest);
    }

    private Event getEventOrThrow(Long eventId, String message) {
        if (message == null || message.isBlank()) {
            message = "В БД не найдено событие с ID = %d.";
        }
        String finalMessage = message;
        return eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundRecordInBD(String.format(finalMessage, eventId)));
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        userService.check(userId, "При отмене заявки на участие в событии " +
                "не найден пользователь ID = %d.");
        Event eventFromDb = getEventOrThrow(requestId, "При отмене заявки на участие " +
                "в событии не найдено событие ID = %d.");
        ParticipationRequest participationRequest = participationRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundRecordInBD(String.format("При отмене заявки на участие " +
                        "в событии не найдена заявка ID = %d.", requestId)));

        if (!Objects.equals(participationRequest.getRequester().getId(), userId)) {
            throw new OperationFailedException(String.format(
                    "Отменить запрос ID = %d нельзя, поскольку Вы не являетесь инициатором запроса.", requestId));
        }
        participationRequest.setStatusRequest(StatusRequest.CANCELED);
        log.info("Выполнена отмена заявки на событие ID = {}, пользователем ID = {}.", requestId, userId);
        return requestMapper.mapToDto(participationRequest);
    }


    @Override
    public List<ParticipationRequestDto> getRequestsForEvent(Long eventId) {
        List<ParticipationRequest> result = participationRequestRepository.findAllByEvent_Id(eventId);
        return requestMapper.mapListToDtoList(result);
    }

    @Override
    public List<ParticipationRequestDto> findRequestByIds(List<Long> requestIds) {
        List<ParticipationRequest> result = participationRequestRepository.findByIdInOrderByIdAsc(requestIds);
        List<ParticipationRequestDto> requestDtos = requestMapper.mapListToDtoList(result);
        log.info("Выдан список заявок на участие в событиях по переданному списку ID заявок.");
        return requestDtos;
    }

    @Override
    @Transactional
    public ParticipationRequestDto updateRequest(Long idRequest, StatusRequest status) {
        Optional<ParticipationRequest> request = participationRequestRepository.findById(idRequest);
        ParticipationRequest result = null;
        if (request.isPresent()) {
            request.get().setStatusRequest(status);
            result = participationRequestRepository.save(request.get());
        }
        return requestMapper.mapToDto(result);
    }

    @Override
    public ParticipationRequestDto getRequestOrThrow(Long reqId, String message) {
        if (message == null || message.isBlank()) {
            message = "Не найден запрос на участие с ID = %d.";
        }
        String finalMessage = message;
        ParticipationRequest request = participationRequestRepository.findById(reqId)
                .orElseThrow(() -> new NotFoundRecordInBD(String.format(finalMessage, reqId)));
        return requestMapper.mapToDto(request);
    }
}
