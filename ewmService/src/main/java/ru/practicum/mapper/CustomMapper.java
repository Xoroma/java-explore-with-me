package ru.practicum.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomMapper {
    public static CompilationDto mapFromNewDtoToModel(Compilation compilation) {
        List<EventShortDto> eventShortDtoList = new ArrayList<>();
        for (Event event : compilation.getEvents()) {
            eventShortDtoList.add(EventShortDto.builder()
                    .annotation(event.getAnnotation())
                    .category(new CategoryDto(event.getCategory().getId(), event.getCategory().getName()))
                    .confirmedRequests(event.getConfirmedRequests())
                    .eventDate(event.getEventDate())
                    .id(event.getId())
                    .initiator(new UserShortDto(event.getInitiator().getId(), event.getInitiator().getName()))
                    .paid(event.getPaid())
                    .title(event.getTitle())
                    .views(event.getViews())
                    .build());
        }

        CompilationDto compilationDto = CompilationDto.builder()
                .id(compilation.getId())
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .events(eventShortDtoList)
                .build();
        return compilationDto;
    }
}
