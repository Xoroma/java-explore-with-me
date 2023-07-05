package ru.practicum.dto.compilation;

import lombok.*;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.validation.UpdateObject;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompilationDto {

    private List<EventShortDto> events;
    private Long id;

    private Boolean pinned;

    @NotBlank(groups = {UpdateObject.class})
    @Size(min = 20, max = 50, message = "Для описания требуется от 20 до 50 символов.", groups = UpdateObject.class)
    private String title;

}
