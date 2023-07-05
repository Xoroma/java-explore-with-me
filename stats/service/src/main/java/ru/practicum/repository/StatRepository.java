package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.dto.StatWithHits;
import ru.practicum.model.Stat;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatRepository extends JpaRepository<Stat, Long> {

    @Query("select new ru.practicum.dto.StatWithHits(s.app.app, s.uri, count (distinct s.ip))" +
            "from Stat s " +
            "where s.timestamp between ?1 and ?2 " +
            "group by s.app.app, s.uri " +
            "order by count (distinct s.ip) desc")
    List<StatWithHits> findAllUniqueWhenUriIsEmpty(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.dto.StatWithHits(s.app.app, s.uri, count (distinct s.ip)) "
            + "from Stat s "
            + "where s.timestamp between ?1 and ?2 "
            + "and s.uri in (?3)"
            + "group by s.app.app, s.uri "
            + "order by count (distinct s.ip) desc ")
    List<StatWithHits> findAllUniqueWhenUriIsNotEmpty(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("select new ru.practicum.dto.StatWithHits(s.app.app, s.uri, count(s.ip)) "
            + "from Stat s where s.timestamp between ?1 and ?2 "
            + " group by s.app.app, s.uri "
            + " order by count(s.ip) desc")
    List<StatWithHits> findAllWhenUriIsEmpty(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.dto.StatWithHits(s.app.app, s.uri, count (s.ip))"
            + "from Stat s "
            + "where s.timestamp between ?1 and ?2 "
            + "and s.uri in (?3)"
            + "group by s.app.app, s.uri "
            + "order by count (s.ip) desc")
    List<StatWithHits> findAllWhenStarEndUris(LocalDateTime start, LocalDateTime end, List<String> uris);
}