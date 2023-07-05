package ru.practicum.service.category;

import ru.practicum.dto.category.CategoryDto;
import ru.practicum.model.Category;

import java.util.List;

public interface CategoryService {


    List<CategoryDto> getAll(int from, int size);


    CategoryDto getById(Long id);


    CategoryDto save(CategoryDto categoryDto);


    CategoryDto update(Long id, CategoryDto categoryDto);


    void delete(Long catId);


    Category getCatOrThrow(Long catId, String message);
}
