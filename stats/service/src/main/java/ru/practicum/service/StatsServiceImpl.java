package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.dto.StatWithHits;
import ru.practicum.dto.StatsDtoForSave;
import ru.practicum.dto.StatsDtoForView;
import ru.practicum.exception.DataException;
import ru.practicum.mapper.StatMapper;
import ru.practicum.model.Application;
import ru.practicum.model.Stat;
import ru.practicum.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsServiceImpl implements StatsService {
    private final StatRepository statRepository;
    private final ApplicationService applicationService;
    private final StatMapper statMapper;

    @Override
    public void save(StatsDtoForSave statDto) {
        Application application = applicationService.getByName(statDto.getApp())
                .orElseGet(() -> applicationService.save(new Application(statDto.getApp())));

        Stat stat = statMapper.mapFromSaveToModel(statDto);
        stat.setUri(statDto.getUri());
        stat.setIp(statDto.getIp());
        stat.setApp(application);
        statRepository.save(stat);
    }

    @Override
    public List<StatsDtoForView> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        List<StatWithHits> result;

        if (end.isBefore(start)) {
            throw new DataException("Дата окончания не может быть раньше даты начала");
        }
        if (unique) {
            if (uris == null || uris.isEmpty()) {
                log.info("Получение статистики: в запросе эндпоинтов нет, unique = true");
                result = statRepository.findAllUniqueWhenUriIsEmpty(start, end);
            } else {
                log.info("Получение статистики: в запросе эндпоинты есть, unique = true");
                result = statRepository.findAllUniqueWhenUriIsNotEmpty(start, end, uris);
            }
        } else {
            if (uris == null || uris.isEmpty()) {
                log.info("Получение статистики: в запросе эндпоинтов нет, unique = false");
                result = statRepository.findAllWhenUriIsEmpty(start, end);
            } else {
                log.info("Получение статистики: в запросе эндпоинты есть, unique = false");
                result = statRepository.findAllWhenStarEndUris(start, end, uris);
            }
        }

        return result.stream().map(statMapper::mapToDtoForView).collect(Collectors.toList());
    }
}
