package ru.practicum.ewm;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class HitDTO {
    private String app;
    private String uri;
    private Long hits;
}
