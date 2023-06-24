package ru.practicum.ewm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.HitDTO;
import ru.practicum.ewm.AnswerDTO;
import ru.practicum.ewm.StatDTO;
import ru.practicum.ewm.exception.NoArgumentException;
import ru.practicum.ewm.mapper.Mapper;
import ru.practicum.ewm.model.Stat;
import ru.practicum.ewm.service.StatService;
import ru.practicum.ewm.storage.StatRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class StatServiceImpl implements StatService {
    private final StatRepository repository;

    @Override
    @Transactional
    public AnswerDTO createStat(StatDTO statDTO) {
        Stat stat = Mapper.fromDTO(statDTO);
        log.info("Event in createStat with statDTO {}", statDTO);
        return Mapper.toDto(repository.save(stat));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HitDTO> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        checkDate(start, end);
        if (uris.isEmpty()) {
            if (!unique) {
                log.info("Event in getStatAll with start {} and end {} unique = false", start, end);
                return repository.getStatAll(start, end)
                        .stream()
                        .map(Mapper::hitToDTO)
                        .collect(Collectors.toList());
            }
            log.info("Event in getStatAllDistinct with start {} and end {} unique = true", start, end);
            return repository.getStatAllDistinct(start, end)
                    .stream()
                    .map(Mapper::hitToDTO)
                    .collect(Collectors.toList());
        }
        if (!unique) {
            log.info("Event in getStat with start {} and end {} and List<uris> {} unique = false", start, end, uris);
            return repository.getStat(start, end, uris)
                    .stream()
                    .map(Mapper::hitToDTO)
                    .collect(Collectors.toList());
        }
        log.info("Event in getStatDistinct with start {} and end {} and List<uris> {} unique = true", start, end, uris);
        return repository.getStatDistinct(start, end, uris)
                .stream()
                .map(Mapper::hitToDTO)
                .collect(Collectors.toList());
    }

    private void checkDate(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start)) {
            log.info("End is before star {} {}", start, end);
            throw new NoArgumentException("End is before star");
        }
    }

}
