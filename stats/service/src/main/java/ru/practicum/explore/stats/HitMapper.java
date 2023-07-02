package ru.practicum.explore.stats;

import lombok.experimental.UtilityClass;
import ru.practicum.explore.stats.model.Hit;

@UtilityClass
public class HitMapper {
    public static Hit toHit(HitDto hitDto) {

        Hit hit = new Hit();

        hit.setApp(hitDto.getApp());
        hit.setUri(hitDto.getUri());
        hit.setIp(hitDto.getIp());
        hit.setTimestamp(hitDto.getTimestamp());

        return hit;
    }

    public static HitDto toHitDto(Hit hit) {
        return new HitDto(hit.getApp(), hit.getUri(), hit.getIp(), hit.getTimestamp());
    }
}
