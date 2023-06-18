package ru.practicum.ewm.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.model.Hit;
import ru.practicum.ewm.model.Stat;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface StatRepository extends JpaRepository<Stat, Long> {

    @Query("select new ru.practicum.ewm.model.Hit(s.app, s.uri, count(s.ip))" +
            "from Stat s " +
            "where s.timestamp between ?1 and ?2 " +
            "and s.uri in ?3 " +
            "group by s.app, s.uri " +
            "order by count(s.ip) desc")
    List<Hit> getStat(LocalDateTime timestampStart, LocalDateTime timestampEnd, Collection<String> uris);

    @Query("select new ru.practicum.ewm.model.Hit(s.app, s.uri, count(distinct s.ip))" +
            "from Stat s " +
            "where s.timestamp between ?1 and ?2 " +
            "and s.uri in ?3 " +
            "group by s.app, s.uri " +
            "order by count(s.ip) desc")
    List<Hit> getStatDistinct(LocalDateTime timestampStart, LocalDateTime timestampEnd, Collection<String> uris);

    @Query("select new ru.practicum.ewm.model.Hit(s.app, s.uri, count(distinct s.ip))" +
            "from Stat s " +
            "where s.timestamp between ?1 and ?2 " +
            "group by s.app, s.uri " +
            "order by count(s.ip) desc")
    List<Hit> getStatAllDistinct(LocalDateTime timestampStart, LocalDateTime timestampEnd);

    @Query("select new ru.practicum.ewm.model.Hit(s.app, s.uri, count(s.ip))" +
            "from Stat s " +
            "where s.timestamp between ?1 and ?2 " +
            "group by s.app, s.uri " +
            "order by count(s.ip) desc")
    List<Hit> getStatAll(LocalDateTime timestampStart, LocalDateTime timestampEnd);

}