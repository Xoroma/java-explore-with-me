package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.user.UserDto;
import ru.practicum.service.user.UserService;
import ru.practicum.validation.CreateObject;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Validated
@RestController("/admin/users")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<UserDto> findAll(@RequestParam(required = false) List<Long> ids,
                                 @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                 @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("GET /admin/users. ids={}, from={}, size={}", ids, from, size);

        return userService.findByIds(ids, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@Validated(CreateObject.class) @RequestBody UserDto userDto) {
        log.info("Добавление нового пользователя. POST /admin/users Body user = {}", userDto);
        return userService.save(userDto);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long userId) {
        log.info("Удаление пользователя с ID = {}.", userId);
        userService.delete(userId);
    }
}
