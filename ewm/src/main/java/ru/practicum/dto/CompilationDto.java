package ru.practicum.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CompilationDto {
    private Set<EventShortDto> events;
    private long id;
    private boolean pinned;
    private String title;
}
