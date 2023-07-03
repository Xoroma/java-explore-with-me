package ru.practicum.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.NotFoundRecordInBD;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> findByIds(List<Long> ids, int from, int size) {
        Pageable pageable = PageRequest.of(from, size, Sort.by("id").ascending());
        List<User> users;
        if (ids == null || ids.isEmpty()) {
            users = userRepository.findAll(pageable).getContent();
            log.info("Выдан список всех пользователей, поскольку список ids пуст.");
        } else {
            users = userRepository.findAllByIdIn(ids, pageable).getContent();
            log.info("Выдан список, состоящий из {} пользователей.", ids.size());
        }
        return users.stream()
                .map(userMapper::mapToUserDto).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public UserDto save(UserDto userDto) {
        User newUser = userMapper.mapToUser(userDto);
        User savedUser = userRepository.save(newUser);
        UserDto result = userMapper.mapToUserDto(savedUser);
        log.info("Выполнено сохранение нового пользователя в БД ID = {}, name = {}.", result.getId(), result.getName());
        return result;
    }

    @Transactional
    @Override
    public void delete(Long userId) {
        UserDto oldUser = check(userId, "При удалении из БД пользователь с ID = %d не найден.");
        userRepository.deleteById(userId);
        log.info("Выполнено удаление пользователя с ID = {} и  name = {}", userId, oldUser.getName());
    }

    @Override
    public User getUserOrThrow(Long userId, String message) {
        if (message == null || message.isBlank()) {
            message = "В БД не найден пользователь с ID = %d.";
        }
        String finalMessage = message;

        return userRepository.findById(userId).orElseThrow(
                () -> new NotFoundRecordInBD(String.format(finalMessage, userId)));
    }

    @Override
    public UserDto check(Long userId, String message) {
        if (message == null || message.isBlank()) {
            message = "В БД не найден пользователь с ID = %d.";
        }

        String finalMessage = message;
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundRecordInBD(String.format(finalMessage, userId)));

        return userMapper.mapToUserDto(user);
    }
}
