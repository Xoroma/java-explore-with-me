package ru.practicum.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.practicum.validation.CreateObject;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;


@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;

    @NotBlank(groups = {CreateObject.class}, message = "При создании пользователя имя не может быть пустым.")
    @Size(min = 2, max = 250, groups = {CreateObject.class})
    private String name;

    @Email(groups = {CreateObject.class}, message = "При создании пользователя email должен быть адресом эл. почты.")
    @NotEmpty(groups = {CreateObject.class}, message = "При создании пользователя email не должен быть пустым или null.")
    @Size(min = 6, max = 254, groups = {CreateObject.class})
    private String email;
}
