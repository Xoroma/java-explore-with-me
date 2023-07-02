package ru.practicum.services.publics;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CompilationDto;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.repositories.CompilationRepository;

import java.util.List;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class PublicCompilationService {
    private CompilationRepository compilationRepository;

    public List<CompilationDto> getCompilations(Boolean pinned, int from, int size) {
        if (pinned != null) {
            return CompilationMapper.toCompilationDtoList(compilationRepository.findAllByPinned(pinned, PageRequest.of(from / size, size)).toList());

        } else {
            return CompilationMapper.toCompilationDtoList(compilationRepository.findAll(PageRequest.of(from / size, size)).toList());
        }
    }

    public CompilationDto getCompilation(long id) {
        return CompilationMapper.toCompilationDto(compilationRepository.findById(id).orElseThrow(() -> new NotFoundException("Compilation with id=" + id + " was not found")));
    }
}
