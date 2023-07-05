package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.service.compilation.CompilationService;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
public class CompilationAdminController {
    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto addCompilation(@Valid @RequestBody NewCompilationDto newCompilationDto) {
        log.info("Добавление новой подборки. POST /admin/compilations {}", newCompilationDto);
        return compilationService.addCompilation(newCompilationDto);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCompilation(@PathVariable Long compId) {
        log.info("Удаление подборки. DELETE /admin/compilations compId {}", compId);
        compilationService.deleteCompilation(compId);
    }


    @PatchMapping("/{compId}")
    @ResponseStatus(HttpStatus.OK)
    public CompilationDto update(@PathVariable Long compId,
                                 @RequestBody @Valid UpdateCompilationRequest updateCompilationRequest) {
        log.info("PATCH /admin/compilations compId {}, {}", compId, updateCompilationRequest);
        return compilationService.updateCompilation(compId, updateCompilationRequest);
    }
}
