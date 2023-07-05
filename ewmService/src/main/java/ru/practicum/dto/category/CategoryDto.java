package ru.practicum.dto.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.validation.CreateObject;
import ru.practicum.validation.UpdateObject;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    private Long id;
    @NotNull(groups = {CreateObject.class, UpdateObject.class}, message = "Имя категории не может быть Null.")
    @NotBlank(groups = {CreateObject.class, UpdateObject.class}, message = "Имя категории не может быть пустым.")
    @Size(min = 1, max = 50, groups = {CreateObject.class}, message = "При создании категории должно быть её название.")
    @Size(min = 1, max = 50, groups = {UpdateObject.class}, message = "При обновлении категории необходимо передать её название.")
    private String name;
}
