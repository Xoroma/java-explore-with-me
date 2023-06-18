package ru.practicum.ewm.mapper;

import ru.practicum.ewm.HitDTO;
import ru.practicum.ewm.AnswerDTO;
import ru.practicum.ewm.StatDTO;
import ru.practicum.ewm.model.Hit;
import ru.practicum.ewm.model.Stat;

public class Mapper {

    public static Stat fromDTO(StatDTO statDTO) {
        Stat stat = new Stat();
        stat.setApp(statDTO.getApp());
        stat.setUri(statDTO.getUri());
        stat.setIp(statDTO.getIp());
        stat.setTimestamp(statDTO.getTimestamp());
        return stat;
    }

    public static AnswerDTO toDto(Stat stat) {
        AnswerDTO answerDTO = new AnswerDTO();
        answerDTO.setId(stat.getId());
        answerDTO.setApp(stat.getApp());
        answerDTO.setUri(stat.getUri());
        answerDTO.setIp(stat.getIp());
        answerDTO.setTimestamp(stat.getTimestamp());
        return answerDTO;
    }

    public static HitDTO hitToDTO(Hit hit) {
        HitDTO hitDTO = new HitDTO();
        hitDTO.setApp(hit.getApp());
        hitDTO.setUri(hit.getUri());
        hitDTO.setHits(hit.getHits());
        return hitDTO;
    }
}
