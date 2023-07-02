package ru.practicum.services.admins;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.dto.UpdateCompilationRequest;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.repositories.CompilationRepository;
import ru.practicum.repositories.EventRepository;

import java.util.HashSet;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class AdminCompilationService {
    private EventRepository eventRepository;
    private CompilationRepository compilationRepository;

    @Transactional
    public CompilationDto createCompilation(NewCompilationDto newCompilation) {
        Compilation compilation = CompilationMapper.toCompilation(newCompilation);
        if (newCompilation.getEvents() == null) {
            newCompilation.setEvents(List.of());
        } else {
            compilation.setEvents(new HashSet<>(eventRepository.findAllById(newCompilation.getEvents())));
        }
        return CompilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }

    @Transactional
    public void deleteCompilation(long compId) {
        compilationRepository.deleteById(compId);
    }

    @Transactional
    public CompilationDto updateCompilation(long compId, UpdateCompilationRequest compilationDto) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() -> new NotFoundException("Compilation with id= " + compId + " was not found"));

        if (compilationDto.getEvents() != null) {
            compilation.setEvents(new HashSet<>(eventRepository.findAllById(compilationDto.getEvents())));
        }
        if (compilationDto.getTitle() != null && !compilationDto.getTitle().isBlank()) {
            compilation.setTitle(compilationDto.getTitle());
        }

        return CompilationMapper.toCompilationDto(compilation);
    }
}
