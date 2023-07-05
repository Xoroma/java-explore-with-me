package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.dto.event.LocationDto;
import ru.practicum.model.Location;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    Location mapToModel(LocationDto locationDto);

    LocationDto mapToDto(Location location);
}
