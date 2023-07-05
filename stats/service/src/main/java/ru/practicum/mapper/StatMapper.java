package ru.practicum.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.dto.StatWithHits;
import ru.practicum.dto.StatsDtoForSave;
import ru.practicum.dto.StatsDtoForView;
import ru.practicum.model.Application;
import ru.practicum.model.Stat;

@Component
public class StatMapper {

    public Stat mapFromSaveToModel(StatsDtoForSave statsDtoForSave) {
        return new Stat(
                statsDtoForSave.getId(),
                new Application(statsDtoForSave.getApp()),
                statsDtoForSave.getIp(),
                statsDtoForSave.getUri(),
                statsDtoForSave.getTimestamp());
    }

    public StatsDtoForSave mapToDtoForSave(Stat stat) {
        return new StatsDtoForSave(
                stat.getId(),
                stat.getApp().getApp(),
                stat.getIp(),
                stat.getUri(),
                stat.getTimestamp());
    }

    public StatsDtoForView mapToDtoForView(StatWithHits statDto) {
        return new StatsDtoForView(
                statDto.getApp(),
                statDto.getUri(),
                Math.toIntExact(statDto.getHits()));
    }
}
