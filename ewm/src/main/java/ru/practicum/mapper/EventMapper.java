package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.Constants;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.Location;
import ru.practicum.dto.NewEventDto;
import ru.practicum.model.Event;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class EventMapper {
    public Event toEvent(NewEventDto eventDto) {
        return Event.builder()
                .annotation(eventDto.getAnnotation())
                .description(eventDto.getDescription())
                .eventDate(eventDto.getEventDate())
                .lat(eventDto.getLocation().getLat())
                .lon(eventDto.getLocation().getLon())
                .paid(eventDto.isPaid())
                .participantLimit(eventDto.getParticipantLimit())
                .requestModeration(eventDto.getRequestModeration())
                .title(eventDto.getTitle())
                .build();
    }

    public EventFullDto toEventDto(Event event) {
        return EventFullDto.builder()
                .id(event.getId())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests())
                .createdOn(event.getCreatedOn())
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .annotation(event.getAnnotation())
                .description(event.getDescription())
                .eventDate(event.getEventDate().format(Constants.FORMATTER))
                .publishedOn(event.getPublishedOn())
                .location(new Location(event.getLat(), event.getLon()))
                .paid(event.isPaid())
                .state(event.getState())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.isRequestModeration())
                .title(event.getTitle())
                .build();
    }

    public EventShortDto toEventShortDto(Event event) {
        return EventShortDto.builder()
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .confirmedRequests(event.getConfirmedRequests())
                .eventDate(event.getEventDate().format(Constants.FORMATTER))
                .id(event.getId())
                .paid(event.isPaid())
                .title(event.getTitle())
                .build();
    }

    public List<EventShortDto> toEventShortDtoList(List<Event> events) {
        if (events == null) {
            return null;
        }
        return events.stream().map(EventMapper::toEventShortDto).collect(Collectors.toList());
    }

    public Set<EventShortDto> toEventShortDtoList(Set<Event> events) {
        if (events == null) {
            return null;
        }
        return events.stream().map(EventMapper::toEventShortDto).collect(Collectors.toSet());
    }

    public List<EventFullDto> toEventFullDtoList(List<Event> events) {
        return events.stream().map(EventMapper::toEventDto).collect(Collectors.toList());
    }
}
