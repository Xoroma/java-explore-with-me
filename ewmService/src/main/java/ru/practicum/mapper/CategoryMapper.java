package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category mapToCategory(CategoryDto categoryDto);

    CategoryDto mapToCategoryDto(Category category);
}
