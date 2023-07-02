package ru.practicum.explore.stats;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.explore.exception.ValidationException;
import ru.practicum.explore.stats.model.Hit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class StatsService {

    private final HitRepository hitRepository;

    public void addHit(HitDto hitDto) {

        Hit hit = hitRepository.save(HitMapper.toHit(hitDto));

        HitMapper.toHitDto(hit);
    }

    public List<VisitDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {

        List<HitShort> hits;

        if (start.isAfter(end)) {
            throw new ValidationException("The start date cannot be earlier than the end date");
        }

        if (uris.size() == 0) {
            if (unique) {
                hits = hitRepository.findHitsByUnique(start, end);
            } else {
                hits = hitRepository.findHits(start, end);
            }
        } else {
            if (unique) {
                hits = hitRepository.findHitsByUriAndUnique(start, end, uris);
            } else {
                hits = hitRepository.findHitsByUri(start, end, uris);
            }
        }

        List<VisitDto> visit = new ArrayList<>();

        for (HitShort hitShort : hits) {
            visit.add(new VisitDto(hitShort.getApp(), hitShort.getUri(), hitShort.getCount()));
        }

        return visit;
    }
}
