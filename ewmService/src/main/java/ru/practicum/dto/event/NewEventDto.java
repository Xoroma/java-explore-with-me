package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.validation.CreateObject;

import javax.persistence.Embedded;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
public class NewEventDto {

    @NotBlank(groups = {CreateObject.class})
    @Size(min = 20, max = 2000, message = "Для описания требуется от 20 до 2000 символов.", groups = {CreateObject.class})
    private String annotation;

    @JsonProperty("category")
    @NotNull(groups = {CreateObject.class})
    @PositiveOrZero(groups = CreateObject.class)
    private Long categoryId;

    @NotBlank(groups = {CreateObject.class})
    @Size(min = 20, max = 7000, message = "Для описания требуется от 20 до 7000 символов.", groups = {CreateObject.class})
    private String description;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;

    @Embedded
    private LocationDto location;

    private boolean paid;

    @PositiveOrZero
    private int participantLimit;

    private boolean requestModeration = true;

    @NotBlank(message = "Заголовок должен быт не null.", groups = {CreateObject.class})
    @Size(min = 3, max = 120, message = "Заголовок должен содержать от 3-х до 120 символов.",
            groups = {CreateObject.class})
    private String title;
}
