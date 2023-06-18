package ru.practicum.ewm.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.HitDTO;
import ru.practicum.ewm.AnswerDTO;
import ru.practicum.ewm.StatDTO;
import ru.practicum.ewm.service.StatService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
@Slf4j
public class Controller {
    private final StatService service;
    private final String pattern = "yyyy-MM-dd HH:mm:ss";

    @GetMapping("/stats")
    public List<HitDTO> getHit(@RequestParam(name = "start") @DateTimeFormat(pattern = pattern) LocalDateTime start,
                               @RequestParam(name = "end") @DateTimeFormat(pattern = pattern) LocalDateTime end,
                               @RequestParam(name = "uris", required = false, defaultValue = "") List<String> uris,
                               @RequestParam(name = "unique", required = false, defaultValue = "false") Boolean unique) {
        log.info("Exception in getHit with start {} and end {} and List<uris> {} unique {}", start, end, uris, unique);
        return service.getStats(start, end, uris, unique);
    }

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public AnswerDTO createStat(@RequestBody @Valid StatDTO statDTO) {
        log.info("Exception in createStat with statDto {}", statDTO);
        return service.createStat(statDTO);
    }
}
