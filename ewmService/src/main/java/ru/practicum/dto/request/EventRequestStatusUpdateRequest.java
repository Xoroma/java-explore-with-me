package ru.practicum.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.model.StatusRequest;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;
    private StatusRequest status;
}
