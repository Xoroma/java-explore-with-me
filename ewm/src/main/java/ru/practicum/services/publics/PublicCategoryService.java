package ru.practicum.services.publics;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.CategoryDto;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.repositories.CategoryRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class PublicCategoryService {
    private CategoryRepository categoryRepository;

    public List<CategoryDto> getCategories(int from, int size) {
        return CategoryMapper.toCategoryDtoList(categoryRepository.findAll(PageRequest.of(from / size, size)).toList());
    }

    public CategoryDto getCategory(long catId) {
        return CategoryMapper.toCategoryDto(categoryRepository.findById(catId).orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found")));
    }
}
