package ru.practicum.services.admins;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.UpdateEventAdminRequest;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Event;
import ru.practicum.repositories.CategoryRepository;
import ru.practicum.repositories.EventRepository;
import ru.practicum.stats.State;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.valid.UpdateEventValid.valid;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class AdminEventService {
    private EventRepository eventRepository;
    private CategoryRepository categoryRepository;

    public List<EventFullDto> searchEvent(List<Long> users,
                                          List<String> states,
                                          List<Long> categories,
                                          LocalDateTime rangeStart,
                                          LocalDateTime rangeEnd,
                                          int from,
                                          int size) {

        Collection<State> stateList = (states != null) ? states.stream().map(State::valueOf).collect(Collectors.toList()) : null;


        return EventMapper.toEventFullDtoList(eventRepository.searchEvents(
                users,
                stateList,
                categories,
                rangeStart,
                rangeEnd,
                PageRequest.of(from / size, size, Sort.by("eventDate"))).toList());
    }

    @Transactional
    public EventFullDto updateEvent(long eventId, UpdateEventAdminRequest eventRequest) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        String state = Optional.ofNullable(eventRequest.getStateAction())
                .map(cat -> eventRequest.getStateAction().name())
                .orElse(null);

        if (eventRequest.getCategory() != null) {
            event.setCategory(categoryRepository.getReferenceById(eventRequest.getCategory()));
        }

        return valid(event, eventRequest, state);
    }
}
