package ru.practicum.services.privates;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.Event;
import ru.practicum.model.Request;
import ru.practicum.repositories.EventRepository;
import ru.practicum.repositories.RequestRepository;
import ru.practicum.services.admins.AdminUserService;
import ru.practicum.stats.State;
import ru.practicum.stats.Status;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class PrivateRequestService {
    private RequestRepository requestRepository;
    private AdminUserService adminService;
    private EventRepository eventRepository;

    public List<ParticipationRequestDto> getRequests(long userId) {
        adminService.getUser(userId);
        List<ParticipationRequestDto> requestDtos = RequestMapper.toRequestDtoList(requestRepository.findByUserId(userId));

        if (requestDtos == null) {
            return List.of();
        }

        return requestDtos;
    }

    @Transactional
    public ParticipationRequestDto createRequest(long userId, long eventId) {
        Request request = Request.builder()
                .created(LocalDateTime.now())
                .user(UserMapper.toUser(adminService.getUser(userId)))
                .build();

        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (!requestRepository.existsByUserIdAndEventId(userId, eventId) && event.getInitiator().getId() != userId &&
                event.getState() == State.PUBLISHED) {
            if (event.getParticipantLimit() == 0) {
                request.setStatus(Status.CONFIRMED);
            } else if (event.getParticipantLimit() > event.getConfirmedRequests()) {
                if (!event.isRequestModeration()) {
                    request.setStatus(Status.CONFIRMED);
                } else {
                    request.setStatus(Status.PENDING);
                }
            } else {
                throw new ConflictException("Your request is not validated");
            }


        } else {
            throw new ConflictException("Your request is not validated");
        }

        request.setEvent(event);
        return RequestMapper.toRequestDto(requestRepository.save(request));
    }

    @Transactional
    public ParticipationRequestDto cancelRequest(long userId, long requestId) {
        Request request = requestRepository.findById(requestId).orElseThrow(() -> new NotFoundException("Request with id=" + requestId + " was not found"));
        if (request.getUser().getId() == userId) {
            request.setStatus(Status.CANCELED);
            return RequestMapper.toRequestDto(request);
        } else {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

    }
}
