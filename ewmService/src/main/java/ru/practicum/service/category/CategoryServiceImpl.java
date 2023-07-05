package ru.practicum.service.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.exception.NotFoundRecordInBD;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryDto> getAll(int from, int size) {
        Pageable pageable = PageRequest.of(from, size, Sort.by("name").ascending());
        List<Category> categories = categoryRepository.findAll(pageable).getContent();
        return categories.stream()
                .map(categoryMapper::mapToCategoryDto).collect(Collectors.toList());
    }


    @Override
    public CategoryDto getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(
                        () -> new NotFoundRecordInBD(
                                String.format("В БД не найдена категория с ID = %d", id)));
        return categoryMapper.mapToCategoryDto(category);
    }

    @Transactional
    @Override
    public CategoryDto save(CategoryDto categoryDto) {
        Category newCategory = categoryMapper.mapToCategory(categoryDto);
        Category savedCategory = categoryRepository.save(newCategory);
        CategoryDto result = categoryMapper.mapToCategoryDto(savedCategory);
        log.info("Выполнено сохранение новой категории в БД ID = {}, name = {}.", result.getId(), result.getName());
        return result;
    }

    @Transactional
    @Override
    public CategoryDto update(Long id, CategoryDto categoryDto) {
        categoryRepository.findById(id).orElseThrow(() -> new NotFoundRecordInBD(
                String.format("При обновлении категории в БД не найдена категория с ID = %d", id))
        );
        Category newCategory = categoryMapper.mapToCategory(categoryDto);
        newCategory.setId(id);
        CategoryDto updatedCategory = categoryMapper.mapToCategoryDto(categoryRepository.save(newCategory));
        log.info("Выполнено сохранение новой категории в БД ID = {}, name = {}.",
                updatedCategory.getId(), updatedCategory.getName());
        return updatedCategory;
    }

    @Transactional
    @Override
    public void delete(Long catId) {
        Category oldCategory = categoryRepository.findById(catId).orElseThrow(
                () -> new NotFoundRecordInBD(String.format(
                        "При удалении категории в БД не найдена категория с ID = %d", catId)));
        categoryRepository.deleteById(catId);
        log.info("Выполнено удаление из БД категории с ID = {}, name = {}.", catId, oldCategory.getName());
    }

    @Override
    public Category getCatOrThrow(Long catId, String message) {
        if (message == null || message.isBlank()) {
            message = "В БД не найдена категория с ID = %d.";
        }
        String finalMessage = message;

        return categoryRepository.findById(catId).orElseThrow(
                () -> new NotFoundRecordInBD(String.format(finalMessage, catId)));
    }
}
