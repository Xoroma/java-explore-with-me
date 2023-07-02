package ru.practicum.services.admins;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CategoryDto;
import ru.practicum.exceptions.ConflictException;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.repositories.CategoryRepository;
import ru.practicum.repositories.EventRepository;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class AdminCategoryService {
    private CategoryRepository categoryRepository;
    private EventRepository eventRepository;

    @Transactional
    public CategoryDto createCategory(CategoryDto categoryDto) {
        try {
            return CategoryMapper.toCategoryDto(categoryRepository.save(CategoryMapper.toCategory(categoryDto)));
        } catch (Exception e) {
            throw new ConflictException("This category name already exists");
        }
    }

    @Transactional
    public CategoryDto updateCategory(long catId, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(catId).orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));
        if (categoryDto.getName().equals(category.getName())) {
            return CategoryMapper.toCategoryDto(category);
        } else if (categoryRepository.existsByName(categoryDto.getName())) {
            throw new ConflictException("This category name already exists");
        }
        category.setName(categoryDto.getName());
        return CategoryMapper.toCategoryDto(category);
    }

    @Transactional
    public void deleteCategory(long catId) {
        if (categoryRepository.existsById(catId)) {
            if (!eventRepository.existsByCategoryId(catId)) {
                categoryRepository.deleteById(catId);
            } else {
                throw new ConflictException("The category is not empty");
            }
        } else {
            throw new NotFoundException("Category with id=" + catId + " was not found");
        }
    }
}
