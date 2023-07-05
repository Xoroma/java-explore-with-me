package ru.practicum.service;

import ru.practicum.dto.StatsDtoForSave;
import ru.practicum.dto.StatsDtoForView;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    void save(StatsDtoForSave statDto);

    List<StatsDtoForView> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique);
}
