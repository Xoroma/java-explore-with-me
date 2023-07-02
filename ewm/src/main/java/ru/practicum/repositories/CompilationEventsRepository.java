package ru.practicum.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.CompilationEvents;

public interface CompilationEventsRepository extends JpaRepository<CompilationEvents, Long> {
}
