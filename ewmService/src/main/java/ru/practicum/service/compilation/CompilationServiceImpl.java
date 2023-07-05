package ru.practicum.service.compilation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.StatsDtoForView;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.exception.NotFoundRecordInBD;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.mapper.CustomMapper;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.util.UtilService;

import java.util.*;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final CompilationMapper compilationMapper;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UtilService utilService;

    @Override
    @Transactional
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = compilationMapper.mapFromNewDtoToModel(newCompilationDto);

        List<Event> events = new ArrayList<>();
        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            events = eventRepository.findAllById(newCompilationDto.getEvents());
        }
        Set<Event> eventSet = new HashSet<>(events);
        compilation.setEvents(eventSet);
        Compilation saved = compilationRepository.save(compilation);

        List<StatsDtoForView> views = utilService.getViews(events);
        events = utilService.fillViews(events, views);

        Map<Event, List<ParticipationRequest>> requests = utilService.prepareConfirmedRequest(events);

        events = utilService.fillConfirmedRequests(events, requests);

        CompilationDto result = compilationMapper.mapToDto(saved);
        result.setEvents(eventMapper.mapFromModelListToShortDtoList(events));

        log.info("Создана подборка из {} событий.", events.size());
        return result;
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        getCompilationOrThrow(compId, "При удалении подборки ID = %d она не найдена в БД.");
        compilationRepository.deleteById(compId);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationDto) {
        Compilation compilation = getCompilationOrThrow(compId,
                "При обновлении подборки ID = %d она не найдена в БД.");
        if (updateCompilationDto.getPinned() != null) {
            compilation.setPinned(updateCompilationDto.getPinned());
        }
        List<Event> events = new ArrayList<>();
        if (updateCompilationDto.getEvents() != null && !updateCompilationDto.getEvents().isEmpty()) {
            events = eventRepository.findAllById(updateCompilationDto.getEvents());
            compilation.setEvents(new HashSet<>(events));
        }
        if (updateCompilationDto.getTitle() != null && !updateCompilationDto.getTitle().isBlank()) {
            compilation.setTitle(updateCompilationDto.getTitle());
        }
        CompilationDto result = compilationMapper.mapToDto(compilation);


        List<StatsDtoForView> views = utilService.getViews(events);
        events = utilService.fillViews(events, views);


        Map<Event, List<ParticipationRequest>> requests = utilService.prepareConfirmedRequest(events);

        events = utilService.fillConfirmedRequests(events, requests);

        result.setEvents(eventMapper.mapFromModelListToShortDtoList(events));
        return compilationMapper.mapToDto(compilation);
    }

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from, size, Sort.by("id").ascending());
        List<Compilation> compilations = compilationRepository.findAllByPinned(pinned, pageable);

        Set<Event> events = new HashSet<>();
        for (Compilation compilation : compilations) {
            events.addAll(compilation.getEvents());
        }
        List<Event> eventList = new ArrayList<>(events);


        List<StatsDtoForView> views = utilService.getViews(eventList);
        eventList = utilService.fillViews(eventList, views);


        Map<Event, List<ParticipationRequest>> requests = utilService.prepareConfirmedRequest(eventList);

        eventList = utilService.fillConfirmedRequests(eventList, requests);

        List<CompilationDto> result = new ArrayList<>();

        for (Compilation c : compilations) {
            List<EventShortDto> eventArrayList = new ArrayList<>();
            CompilationDto compilationDto = CustomMapper.mapFromNewDtoToModel(c);
            compilationDto.setEvents(new ArrayList<>());
            Set<Event> eventSet = c.getEvents();
            for (Event ev : eventSet) {
                for (Event evFromList : eventList) {
                    if (evFromList != null && evFromList.getId().equals(ev.getId())) {
                        eventArrayList.add(eventMapper.mapToShortDto(evFromList));
                    }
                }
            }
            compilationDto.setEvents(eventArrayList);
            result.add(compilationDto);
        }
        return result;
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = getCompilationOrThrow(compId,
                "При получении подборки по ID = %d она не найдена в БД.");

        List<Event> events = new ArrayList<>(compilation.getEvents());


        Set<Event> eventSet = new HashSet<>(events);

        compilation.setEvents(eventSet);

        Compilation saved = compilationRepository.save(compilation);

        List<StatsDtoForView> views = utilService.getViews(events);
        events = utilService.fillViews(events, views);

        Map<Event, List<ParticipationRequest>> requests = utilService.prepareConfirmedRequest(events);

        events = utilService.fillConfirmedRequests(events, requests);

        CompilationDto result = compilationMapper.mapToDto(saved);
        result.setEvents(eventMapper.mapFromModelListToShortDtoList(events));

        log.info("Создана подборка из {} событий.", events.size());

        return result;
    }

    private Compilation getCompilationOrThrow(Long compId, String message) {
        if (message == null || message.isBlank()) {
            message = "Подборка с ID = %d не найдена в БД.";
        }
        String finalMessage = message;
        return compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundRecordInBD(String.format(finalMessage, compId)));
    }
}
