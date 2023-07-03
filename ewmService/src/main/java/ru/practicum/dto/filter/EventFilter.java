package ru.practicum.dto.filter;

import lombok.*;
import ru.practicum.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventFilter {
    private List<Long> userIds;

    private List<EventState> states;
    private List<Long> categories;
    private Boolean paid;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private String text;
    private Boolean onlyAvailable;
}
