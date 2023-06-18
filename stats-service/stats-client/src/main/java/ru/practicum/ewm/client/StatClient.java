package ru.practicum.ewm.client;


import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.practicum.ewm.HitDTO;
import ru.practicum.ewm.AnswerDTO;
import ru.practicum.ewm.StatDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
public class StatClient {
    private final WebClient client;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatClient() {
        String url = "http://localhost:9090";
        this.client = WebClient.builder()
                .baseUrl(url)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public AnswerDTO createStat(StatDTO statDTO) {
        return client
                .post()
                .uri("/hit")
                .body(statDTO, StatDTO.class)
                .retrieve()
                .bodyToMono(AnswerDTO.class)
                .block();
    }

    public ResponseEntity<List<HitDTO>> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        String startDate = start.format(timeFormatter);
        String endDate = end.format(timeFormatter);

        return client
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/stats")
                        .queryParam("start", startDate)
                        .queryParam("end", endDate)
                        .queryParam("uris", uris)
                        .queryParam("unique", unique.toString())
                        .build())
                .retrieve()
                .toEntityList(HitDTO.class)
                .block();
    }
}

