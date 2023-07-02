package ru.practicum.controllers.admins;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.dto.UpdateCompilationRequest;
import ru.practicum.services.admins.AdminCompilationService;

import javax.validation.Valid;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/admin")
public class AdminCompilationController {
    private AdminCompilationService adminCompilationService;

    @PostMapping("/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createCompilation(@Valid @RequestBody NewCompilationDto newCompilation) {
        log.info("Добавление новой подборки событий");
        return adminCompilationService.createCompilation(newCompilation);
    }

    @DeleteMapping("/compilations/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable long compId) {
        log.info("Удаление подборки id: {}", compId);
        adminCompilationService.deleteCompilation(compId);
    }

    @PatchMapping("/compilations/{compId}")
    public CompilationDto updateCompilation(@PathVariable long compId, @Valid @RequestBody UpdateCompilationRequest compilation) {
        log.info("Обновление подборки с id: {}", compId);
        return adminCompilationService.updateCompilation(compId, compilation);
    }
}
