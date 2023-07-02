package ru.practicum.dto;


import lombok.Data;
import ru.practicum.stats.Status;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;
    private Status status;
}
