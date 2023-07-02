package ru.practicum.explore.stats;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.explore.stats.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface HitRepository extends JpaRepository<Hit, Long> {

    @Query(value = "select APP, URI, count(URI) as count" +
            " from HITS" +
            " where ?1 <= CREATED_DATE and ?2 >= CREATED_DATE" +
            " group by APP, URI " +
            " order by count DESC ", nativeQuery = true)
    List<HitShort> findHits(LocalDateTime startTime, LocalDateTime endTime);

    @Query(value = "select APP, URI, count(distinct IP) as count" +
            " from HITS" +
            " where ?1 <= CREATED_DATE and ?2 >= CREATED_DATE" +
            " group by APP, URI" +
            " order by count DESC ", nativeQuery = true)
    List<HitShort> findHitsByUnique(LocalDateTime startTime, LocalDateTime endTime);

    @Query(value = "select APP, URI, count(URI) as count" +
            " from HITS" +
            " where ?1 <= CREATED_DATE and ?2 >= CREATED_DATE and URI IN ?3" +
            " group by APP, URI" +
            " order by count DESC ", nativeQuery = true)
    List<HitShort> findHitsByUri(LocalDateTime startTime, LocalDateTime endTime, List<String> uris);

    @Query(value = "select APP, URI, count(distinct IP) as count" +
            " from HITS" +
            " where ?1 <= CREATED_DATE and ?2 >= CREATED_DATE and URI IN ?3" +
            " group by APP, URI" +
            " order by count DESC ", nativeQuery = true)
    List<HitShort> findHitsByUriAndUnique(LocalDateTime startTime, LocalDateTime endTime, List<String> uris);

}
