package ru.practicum.explore.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VisitDto {
    private String app;
    private String uri;
    private long hits;
}
