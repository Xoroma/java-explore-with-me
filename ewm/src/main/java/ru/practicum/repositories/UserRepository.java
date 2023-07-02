package ru.practicum.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByName(String name);
}
