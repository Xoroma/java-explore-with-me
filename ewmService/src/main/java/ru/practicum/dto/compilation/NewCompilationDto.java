package ru.practicum.dto.compilation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class NewCompilationDto {

    private List<Long> events;

    private boolean pinned;

    @NotBlank
    @Size(max = 50)
    private String title;
}
