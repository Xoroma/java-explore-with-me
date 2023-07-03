package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.model.StateAction;
import ru.practicum.validation.UpdateObject;

import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UpdateEventUserRequest {

    @Size(min = 20, max = 2000, groups = {UpdateObject.class})
    private String annotation;
    private Long category;
    @Size(min = 20, max = 7000, groups = {UpdateObject.class})
    private String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private LocationDto location;
    private Boolean paid;
    @PositiveOrZero(groups = {UpdateObject.class})
    private Integer participantLimit;
    private Boolean requestModeration = true;
    private StateAction stateAction;
    @Size(min = 3, max = 120, message = "Заголовок должен содержать от 3-х до 120 символов.", groups = {UpdateObject.class})
    private String title;
}
