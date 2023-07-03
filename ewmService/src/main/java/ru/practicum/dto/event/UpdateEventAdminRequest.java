package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.practicum.model.StateAction;
import ru.practicum.validation.UpdateObject;

import javax.persistence.Embedded;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
public class UpdateEventAdminRequest {

    @Size(min = 20, max = 2000, message = "Для описания требуется от 20 до 2000 символов.", groups = {UpdateObject.class})
    private String annotation;

    private Long category;
    @Size(min = 20, max = 7000, message = "Для описания требуется от 20 до 7000 символов.", groups = {UpdateObject.class})
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;


    @Embedded
    private LocationDto location;

    private Boolean paid;

    @PositiveOrZero(groups = {UpdateObject.class})
    private Integer participantLimit;


    private Boolean requestModeration;

    private StateAction stateAction;

    @Size(min = 3, max = 120, message = "Заголовок должен содержать от 3-х до 120 символов.",
            groups = {UpdateObject.class})
    private String title;
}
