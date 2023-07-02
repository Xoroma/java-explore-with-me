package ru.practicum.controllers.publics;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CompilationDto;
import ru.practicum.services.publics.PublicCompilationService;

import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/compilations")
@Validated
public class PublicCompilationController {
    private PublicCompilationService publicCompilationService;

    @GetMapping
    public List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                @RequestParam(defaultValue = "0") int from,
                                                @RequestParam(defaultValue = "10") int size) {
        log.info("Получение списка подборок");
        return publicCompilationService.getCompilations(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilation(@Positive @PathVariable long compId) {
        log.info("Получение подборки по id: {}", compId);
        return publicCompilationService.getCompilation(compId);
    }
}

