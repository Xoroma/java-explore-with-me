package ru.practicum.service.event;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.WebClientService;
import ru.practicum.dto.StatsDtoForView;
import ru.practicum.dto.event.*;
import ru.practicum.dto.filter.EventFilter;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.*;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.LocationMapper;
import ru.practicum.model.*;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.category.CategoryService;
import ru.practicum.service.request.ParticipationRequestService;
import ru.practicum.service.user.UserService;
import ru.practicum.util.QPredicates;
import ru.practicum.util.UtilService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.model.QEvent.event;

@Slf4j
@Service
@RequiredArgsConstructor
@Import({WebClientService.class})
public class EventServiceImpl implements EventService {

    private final EventMapper eventMapper;
    private final UserService userService;
    private final EventRepository eventRepository;
    private final CategoryService categoryService;
    private final UtilService utilService;
    private final WebClientService statsClient;
    private final ParticipationRequestService participationRequestService;
    private final LocationMapper locationMapper;
    private static final String nameApp = "ewmService";

    public List<EventShortDto> getMyEvents(Long userId, Integer from, Integer size) {
        UserDto userFromDb = userService.check(userId, "Не найден пользователь с ID = {} в БД при получении" +
                " событий, добавленных текущим пользователем.");
        Pageable pageable = PageRequest.of(from, size, Sort.by("id").ascending());

        Predicate predicate = QEvent.event.initiator.id.ne(userId);
        List<EventShortDto> result = eventRepository.findAll(predicate, pageable)
                .stream().map(eventMapper::mapToShortDto).collect(Collectors.toList());
        log.info("Выдан результат запроса о своих событиях ({} событий), для пользователя с ID = {} и name = {}.",
                result.size(), userFromDb.getId(), userFromDb.getName());
        return result;
    }

    @Override
    @Transactional
    public EventFullDto create(Long userId, NewEventDto newEventDto) {
        User initiator = userService.getUserOrThrow(userId,
                "При создании события не найден пользователь ID = {}.");
        Category category = categoryService.getCatOrThrow(newEventDto.getCategoryId(),
                "При создании события не найдена категория с ID = {}.");

        checkDateEvent(newEventDto.getEventDate(), 2);

        Event newEvent = eventMapper.mapFromNewToModel(newEventDto);
        newEvent.setInitiator(initiator);
        newEvent.setCategory(category);
        newEvent.setEventState(EventState.PENDING);
        newEvent.setCreatedOn(LocalDateTime.now());
        Event savedEvent = eventRepository.save(newEvent);
        EventFullDto result = eventMapper.mapFromModelToFullDtoWhenCreate(savedEvent, 0, 0);
        log.info("Создано событие в БД с ID = {} и кратким описанием: {}.", savedEvent.getId(),
                savedEvent.getAnnotation());
        return result;
    }

    @Override
    public List<EventFullDto> getEventsForAdmin(List<Long> userIds, List<EventState> states, List<Long> categories,
                                                LocalDateTime rangeStart, LocalDateTime rangeEnd, String text,
                                                Integer from, Integer size) {

        log.info("GET /admin/events users={},states={},categories={},\nrangeStart={},rangeEnd={}" +
                ",from={},size={}", userIds, states, categories, rangeStart, rangeEnd, from, size);
        Pageable pageable = PageRequest.of(from, size, Sort.by("id").ascending());

        EventFilter eventFilter = EventFilter.builder()
                .userIds(userIds)
                .states(states)
                .categories(categories)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .text("%" + text.trim().toLowerCase() + "%").build();

        if (userIds == null && states == null && categories == null && rangeStart == null && rangeEnd == null && text.isBlank()) {
            return eventMapper.mapFromModelListToFullDtoList(eventRepository.findAll(pageable).toList());
        }

        BooleanBuilder booleanBuilderForStates = new BooleanBuilder();
        if (states != null) {
            for (EventState state : states) {
                BooleanExpression eq = event.eventState.eq(state);
                booleanBuilderForStates.andAnyOf(eq);
            }
        }

        Predicate predicateForText = null;
        if (eventFilter.getText() != null && eventFilter.getText().isBlank()) {
            predicateForText = QPredicates.builder()
                    .add(event.annotation.likeIgnoreCase(eventFilter.getText()))
                    .add(event.description.likeIgnoreCase(eventFilter.getText()))
                    .buildOr();
        }


        QPredicates qPredicatesWithoutStatesAndText = QPredicates.builder()
                .add(eventFilter.getUserIds(), event.initiator.id::in)
                .add(eventFilter.getCategories(), event.category.id::in)
                .add(eventFilter.getPaid(), event.paid::eq)
                .add(eventFilter.getRangeStart(), event.eventDate::after)
                .add(eventFilter.getRangeEnd(), event.eventDate::before);

        Predicate filterForAll = qPredicatesWithoutStatesAndText
                .add(predicateForText)
                .add(booleanBuilderForStates.getValue())
                .buildAnd();

        List<Event> events = eventRepository.findAll(filterForAll, pageable).toList();


        List<StatsDtoForView> stats = utilService.getViews(events);

        events = utilService.fillViews(events, stats);


        Map<Event, List<ParticipationRequest>> confirmedRequests = utilService.prepareConfirmedRequest(events);

        events = utilService.fillConfirmedRequests(events, confirmedRequests);


        List<EventFullDto> list = eventMapper.mapFromModelListToFullDtoList(events);
        log.info("Администратору отправлен список событий из {} событий.", list.size());
        return list;
    }

    @Override
    public List<EventShortDto> getEventsForAll(String text, List<Long> categoriesIds, Boolean paid,
                                               LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                               Boolean onlyAvailable, String sort,
                                               Integer from, Integer size,
                                               HttpServletRequest httpServletRequest) {
        log.info("Получение событий для всех с помощью фильтра. GET /events text:{},\ncategories:{}," +
                        "paid:{},rangeStart:{},rangeEnd:{},\nonlyAvailable:{},sort:{},from:{}, size:{}",
                text, categoriesIds, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
        Sort sortForResponse;
        if (rangeStart != null && rangeEnd != null) {
            if (rangeStart.isAfter(rangeEnd)) {
                throw new InvalidDateTimeException("Дата окончания мероприятия не может быть раньше даты начала");
            }
        }


        if (sort == null || sort.isBlank() || sort.equalsIgnoreCase("EVENT_DATE")) {
            sortForResponse = Sort.by("eventDate").ascending();
        } else if (!sort.isBlank() && sort.equals("VIEWS")) {
            sortForResponse = Sort.by("views").ascending();
        } else {
            throw new InvalidSortException(String.format("Ошибка в методе получения событий с возможностью " +
                    "фильтрации. Параметр сортировки имеет не верное значение = %s.", sort));
        }
        try {
            statsClient.save(
                    nameApp,
                    httpServletRequest.getRequestURI(),
                    httpServletRequest.getRemoteAddr(),
                    LocalDateTime.now()
            );
            log.info("Sending statistics was successful");
        } catch (StatsException e) {
            log.error("Sending statistics failed");
        }

        Pageable pageable = PageRequest.of(from, size, sortForResponse);

        EventFilter eventFilter = EventFilter.builder()
                .text("%" + text.trim().toLowerCase() + "%")
                .categories(categoriesIds)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .build();

        Predicate predicateForText = null;
        if (!eventFilter.getText().isBlank()) {
            predicateForText = QPredicates.builder()
                    .add(event.annotation.likeIgnoreCase(eventFilter.getText()))
                    .add(event.description.likeIgnoreCase(eventFilter.getText()))
                    .buildOr();
        }


        QPredicates qPredicatesWithoutStatesAndText = QPredicates.builder()
                .add(eventFilter.getCategories(), event.category.id::in)
                .add(eventFilter.getPaid(), event.paid::eq)
                .add(eventFilter.getRangeStart(), event.eventDate::after)
                .add(eventFilter.getRangeEnd(), event.eventDate::before);

        Predicate filterForAll = qPredicatesWithoutStatesAndText
                .add(predicateForText)
                .buildAnd();
        List<Event> events = eventRepository.findAll(filterForAll, pageable).toList();


        List<StatsDtoForView> stats = utilService.getViews(events);

        events = utilService.fillViews(events, stats);


        Map<Event, List<ParticipationRequest>> confirmedRequests = utilService.prepareConfirmedRequest(events);

        events = utilService.fillConfirmedRequests(events, confirmedRequests);

        log.info("Выдан список событий ({} шт) по запросу с фильтрами.", events.size());

        return eventMapper.mapFromModelListToShortDtoList(events);
    }

    private EventFilter fillFilter(List<Long> userIds,
                                   List<EventState> states,
                                   List<Long> categories,
                                   LocalDateTime rangeStart,
                                   LocalDateTime rangeEnd,
                                   String text) {
        return EventFilter.builder()
                .userIds(userIds)
                .states(states)
                .categories(categories)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .text(text)
                .build();
    }

    @Override
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event eventFromDb = getEventOrThrow(eventId, "При обновлении события админом " +
                "не найдено событие в БД с ID = %d.");

        checkStateAction(eventFromDb, updateEventAdminRequest);

        eventFromDb = updateEventsFieldsByAdmin(eventFromDb, updateEventAdminRequest);

        eventFromDb = eventRepository.save(eventFromDb);

        List<Event> events = List.of(eventFromDb);
        List<StatsDtoForView> stats = utilService.getViews(events);

        events = utilService.fillViews(events, stats);


        Map<Event, List<ParticipationRequest>> confirmedRequests = utilService.prepareConfirmedRequest(events);

        events = utilService.fillConfirmedRequests(events, confirmedRequests);
        Event result = events.get(0);
        log.info("Выполнено обновление события с ID = {}.", eventId);
        return eventMapper.mapFromModelToFullDto(result);
    }


    @Override
    public EventFullDto getEventById(Long eventId, HttpServletRequest httpServletRequest) {
        log.info("Готовим ответ на запрос события по ID = {} в общедоступном режиме ", eventId);

        Event event = getEventOrThrow(eventId, "При получении события в общедоступном режиме " +
                "не найдено событие в БД с ID = %d.");
        if (event.getEventState() != EventState.PUBLISHED)
            throw new NotFoundRecordInBD("Посмотреть можно только опубликованное событие.");

        List<Event> events = List.of(event);
        List<StatsDtoForView> stats = utilService.getViews(events);

        events = utilService.fillViews(events, stats);


        Map<Event, List<ParticipationRequest>> confirmedRequests = utilService.prepareConfirmedRequest(events);

        events = utilService.fillConfirmedRequests(events, confirmedRequests);
        Event result = events.get(0);
        statsClient.save(nameApp, httpServletRequest.getRequestURI(),
                httpServletRequest.getRemoteAddr(), LocalDateTime.now());
        log.info("Отправлен ответ на запрос события по ID = {} в общедоступном режиме ", eventId);
        return eventMapper.mapFromModelToFullDto(result);
    }

    @Override
    public EventFullDto getMyEventById(Long userId, Long eventId) {
        log.info("Готовим ответ на запрос пользователя с ID = {} события по ID = {} в приватном режиме.",
                userId, eventId);

        Event event = getEventOrThrow(eventId, "При получении пользователем с ID = "
                + userId + " собственного события с ID = %d в приватном режиме " +
                "это событие не найдено в БД .");


        List<Event> events = List.of(event);
        List<StatsDtoForView> stats = utilService.getViews(events);

        events = utilService.fillViews(events, stats);


        Map<Event, List<ParticipationRequest>> confirmedRequests = utilService.prepareConfirmedRequest(events);

        events = utilService.fillConfirmedRequests(events, confirmedRequests);
        Event result = events.get(0);
        log.info("Отправлен ответ на запрос собственного события по ID = {} в приватном режиме ", eventId);
        return eventMapper.mapFromModelToFullDto(result);
    }

    @Override
    public EventFullDto cancelEvent(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiator_Id(eventId, userId)
                .orElseThrow(() -> new NotFoundRecordInBD(String.format("При отмене события с ID = %d " +
                        "пользователем с ID = %d событие не найдено.", eventId, userId)));
        if (event.getEventState() != EventState.PENDING)
            throw new FoundConflictInDB("Отменить можно только опубликованное событие.");

        event.setEventState(EventState.CANCELED);
        event = eventRepository.save(event);
        log.info("Статус события с ID = {} изменён пользователем с ID = {} на \"Отменено - CANCELED\".",
                eventId, userId);

        List<Event> events = List.of(event);
        List<StatsDtoForView> stats = utilService.getViews(events);

        events = utilService.fillViews(events, stats);


        Map<Event, List<ParticipationRequest>> confirmedRequests = utilService.prepareConfirmedRequest(events);

        events = utilService.fillConfirmedRequests(events, confirmedRequests);
        Event result = events.get(0);
        log.info("Отправлен ответ на запрос об отмене события с ID = {} пользователем с ID = {}.", eventId, userId);
        return eventMapper.mapFromModelToFullDto(result);
    }

    @Override
    public EventFullDto updateEventUser(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        User userFromDb = userService.getUserOrThrow(userId,
                "При обновлении события не найден пользователь ID = {}.");
        Event eventFromDb = getEventOrThrow(eventId, "При обновлении в приватном режиме события с ID = %d " +
                "пользователем с ID = " + userId + " событие не найдено в БД.");
        Category category = null;
        if (updateEventUserRequest.getCategory() != null) {
            category = categoryService.getCatOrThrow(updateEventUserRequest.getCategory(),
                    "При обновлении в приватном режиме события с ID = " + eventId
                            + "пользователем с ID = " + userId + " не найдена категория с ID = {}.");
        }

        if (!Objects.equals(userFromDb.getId(), eventFromDb.getInitiator().getId())) {
            throw new OperationFailedException("Пользователь не является инициатором события");
        }
        if (eventFromDb.getEventState().equals(EventState.PUBLISHED)) {
            throw new OperationFailedException("Невозможно обновить событие, поскольку оно уже опубликовано.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (updateEventUserRequest.getEventDate() != null) {
            checkDateEvent(updateEventUserRequest.getEventDate(), 1);
        }

        eventFromDb = updateEventsFieldsByUser(eventFromDb, updateEventUserRequest);
        Event savedEvent = eventRepository.save(eventFromDb);

        List<Event> events = List.of(savedEvent);
        List<StatsDtoForView> stats = utilService.getViews(events);

        events = utilService.fillViews(events, stats);


        Map<Event, List<ParticipationRequest>> confirmedRequests = utilService.prepareConfirmedRequest(events);

        events = utilService.fillConfirmedRequests(events, confirmedRequests);
        Event result = events.get(0);
        log.info("Выполнено обновление события с ID = {}.", eventId);

        return eventMapper.mapFromModelToFullDto(result);
    }

    @Override
    public List<ParticipationRequestDto> getRequestsEvent(Long userId, Long eventId) {
        List<ParticipationRequestDto> result = new ArrayList<>();
        userService.getUserOrThrow(userId,
                "При получении запросов на участие в событиях текущего пользователя " +
                        "не найден пользователь с ID = %d.");
        getEventOrThrow(eventId, "При получении запросов на участие в событиях текущего пользователя с ID = "
                + userId + " события с ID = %d событие не найдено в БД.");
        result = participationRequestService.getRequestsForEvent(eventId);
        return result;
    }

    @Override
    public EventFullDto publish(Long eventId) {
        Event eventFromDb = getEventOrThrow(eventId, "При попытке публикации события " +
                "ID = %d оно не найдено в БД.");

        if (eventFromDb.getEventState() != EventState.PENDING)
            throw new OperationFailedException("Публиковать можно только события в статусе ожидания.");

        checkDateEvent(eventFromDb.getEventDate(), 1);

        eventFromDb.setEventState(EventState.PUBLISHED);
        eventFromDb.setPublishedOn(LocalDateTime.now());
        Event saved = eventRepository.save(eventFromDb);
        log.info("Опубликовано событие с ID = {}.", eventId);

        return eventMapper.mapFromModelToFullDto(saved);
    }

    @Override
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId,
                                                              EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        User userFromDb = userService.getUserOrThrow(userId,
                "При изменении статусов запросов не найден пользователь с ID = %d.");
        Event eventFromDb = getEventOrThrow(eventId, "При изменении статусов запросов события с ID = %d " +
                "пользователем с ID = " + userId + " событие не найдено в БД.");

        List<Event> events = setViewsAndConfirmedRequests(List.of(eventFromDb));

        eventFromDb = events.get(0);

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();

        if (eventFromDb.getParticipantLimit() == 0 || !eventFromDb.getRequestModeration()) {
            return result;
        }
        if (eventFromDb.getParticipantLimit() <= eventFromDb.getConfirmedRequests()) {
            throw new OperationFailedException("Закончились свободные места на участие в событии или " +
                    "запрос на участие уже был подтверждён.");
        }

        List<ParticipationRequestDto> requests =
                participationRequestService.findRequestByIds(eventRequestStatusUpdateRequest.getRequestIds());
        for (ParticipationRequestDto request : requests) {
            if (request.getStatus() != StatusRequest.PENDING) {
                throw new OperationFailedException("Статус запроса не \"в ожидании\"");
            }
            if (checkParticipantLimit(eventFromDb) &&
                    eventRequestStatusUpdateRequest.getStatus() == StatusRequest.CONFIRMED) {
                request.setStatus(StatusRequest.CONFIRMED);
                result.getConfirmedRequests().add(request);
                participationRequestService.updateRequest(request.getId(), StatusRequest.CONFIRMED);
                events = setViewsAndConfirmedRequests(events);
            } else {
                request.setStatus(StatusRequest.REJECTED);
                result.getRejectedRequests().add(request);
            }

        }
        return result;
    }

    private boolean checkParticipantLimit(Event event) {
        return event.getParticipantLimit() - event.getConfirmedRequests() > 0;
    }

    public Event getEventOrThrow(Long eventId, String message) {
        if (message == null || message.isBlank()) {
            message = "В БД не найдено событие с ID = %d.";
        }
        String finalMessage = message;
        return eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundRecordInBD(String.format(finalMessage, eventId)));
    }


    private void checkStateAction(Event oldEvent, UpdateEventAdminRequest newEvent) {

        if (newEvent.getStateAction() == StateAction.PUBLISH_EVENT) {
            if (oldEvent.getEventState() != EventState.PENDING) {
                throw new OperationFailedException("Невозможно опубликовать событие, поскольку его можно " +
                        "публиковать, только если оно в состоянии ожидания публикации.");
            }
        }
        if (newEvent.getStateAction() == StateAction.REJECT_EVENT) {
            if (oldEvent.getEventState() == EventState.PUBLISHED) {
                throw new OperationFailedException("Событие опубликовано, поэтому отменить его невозможно.");
            }
        }
        if (oldEvent.getEventState().equals(EventState.CANCELED)
                && newEvent.getStateAction().equals(StateAction.PUBLISH_EVENT)) {
            throw new OperationFailedException("Невозможно отменить опубликованное событие.");
        }
    }

    private void checkDateEvent(LocalDateTime newEventDateTime, int plusHours) {

        LocalDateTime now = LocalDateTime.now().plusHours(plusHours);
        if (now.isAfter(newEventDateTime)) {
            throw new InvalidDateTimeException(String.format("Error 400. Field: eventDate. Error: Дата начала" +
                    " события, должна быть позже текущего момента на %s ч.", plusHours));
        }
    }

    private Event updateEventsFieldsByAdmin(Event oldEvent, UpdateEventAdminRequest updateEvent) {
        if (updateEvent.getAnnotation() != null && !updateEvent.getAnnotation().isBlank()) {
            oldEvent.setAnnotation(updateEvent.getAnnotation());
        }
        if (updateEvent.getCategory() != null) {
            oldEvent.getCategory().setId(updateEvent.getCategory());
        }
        if (updateEvent.getDescription() != null && !updateEvent.getDescription().isBlank()) {
            oldEvent.setDescription(updateEvent.getDescription());
        }
        if (updateEvent.getEventDate() != null) {
            checkDateEvent(updateEvent.getEventDate(), 1);
            oldEvent.setEventDate(updateEvent.getEventDate());
        }
        if (updateEvent.getLocation() != null) {
            oldEvent.setLocation(locationMapper.mapToModel(updateEvent.getLocation()));
        }
        if (updateEvent.getPaid() != null) {
            oldEvent.setPaid(updateEvent.getPaid());
        }
        if (updateEvent.getParticipantLimit() != null) {
            oldEvent.setParticipantLimit(updateEvent.getParticipantLimit());
        }
        if (updateEvent.getRequestModeration() != null) {
            oldEvent.setRequestModeration(updateEvent.getRequestModeration());
        }
        if (StateAction.CANCEL_REVIEW.equals(updateEvent.getStateAction()) ||
                StateAction.REJECT_EVENT.equals(updateEvent.getStateAction())) {
            oldEvent.setEventState(EventState.CANCELED);
        }
        if (StateAction.SEND_TO_REVIEW.equals(updateEvent.getStateAction())) {
            oldEvent.setEventState(EventState.PENDING);
        }
        if (StateAction.PUBLISH_EVENT.equals(updateEvent.getStateAction())) {
            oldEvent.setEventState(EventState.PUBLISHED);
            oldEvent.setPublishedOn(LocalDateTime.now());
        }
        if (updateEvent.getTitle() != null && !updateEvent.getTitle().isBlank()) {
            oldEvent.setTitle(updateEvent.getTitle());
        }
        return oldEvent;
    }

    private Event updateEventsFieldsByUser(Event oldEvent, UpdateEventUserRequest updateEvent) {
        if (updateEvent.getCategory() != null) {
            oldEvent.getCategory().setId(updateEvent.getCategory());
        }
        if (updateEvent.getAnnotation() != null && !updateEvent.getAnnotation().isBlank()) {
            oldEvent.setAnnotation(updateEvent.getAnnotation());
        }
        if (updateEvent.getEventDate() != null) {
            oldEvent.setEventDate(updateEvent.getEventDate());
        }
        if (updateEvent.getLocation() != null) {
            oldEvent.setLocation(locationMapper.mapToModel(updateEvent.getLocation()));
        }
        if (updateEvent.getDescription() != null && !updateEvent.getDescription().isBlank()) {
            oldEvent.setDescription(updateEvent.getDescription());
        }
        if (updateEvent.getRequestModeration() != null) {
            oldEvent.setRequestModeration(updateEvent.getRequestModeration());
        }
        if (updateEvent.getPaid() != null) {
            oldEvent.setPaid(updateEvent.getPaid());
        }
        if (updateEvent.getParticipantLimit() != null) {
            oldEvent.setParticipantLimit(updateEvent.getParticipantLimit());
        }
        if (StateAction.CANCEL_REVIEW.equals(updateEvent.getStateAction())) {
            oldEvent.setEventState(EventState.CANCELED);
        }
        if (StateAction.SEND_TO_REVIEW.equals(updateEvent.getStateAction())) {
            oldEvent.setEventState(EventState.PENDING);
        }
        if (StateAction.PUBLISH_EVENT.equals(updateEvent.getStateAction())) {
            oldEvent.setEventState(EventState.PUBLISHED);
            oldEvent.setPublishedOn(LocalDateTime.now());
        }
        if (updateEvent.getTitle() != null && !updateEvent.getTitle().isBlank()) {
            oldEvent.setTitle(updateEvent.getTitle());
        }
        return oldEvent;
    }

    private void saveStat(HttpServletRequest request) {
        try {
            statsClient.save(
                    nameApp,
                    request.getRequestURI(),
                    request.getRemoteAddr(),
                    LocalDateTime.now()
            );
            log.info("Информация о запросе по этому url = {}: сохранена.", request.getRequestURI());
        } catch (StatsException e) {
            log.error("Ошибка в работе клиента статистики.");
        }
    }

    private List<Event> setViewsAndConfirmedRequests(List<Event> events) {

        List<StatsDtoForView> stats = utilService.getViews(events);
        events = utilService.fillViews(events, stats);

        Map<Event, List<ParticipationRequest>> confirmedRequests = utilService.prepareConfirmedRequest(events);
        events = utilService.fillConfirmedRequests(events, confirmedRequests);
        return events;
    }

}
