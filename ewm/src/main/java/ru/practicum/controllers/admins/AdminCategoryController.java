package ru.practicum.controllers.admins;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CategoryDto;
import ru.practicum.services.admins.AdminCategoryService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/admin")
@Validated
public class AdminCategoryController {
    private AdminCategoryService adminCategoryService;

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@Valid @RequestBody CategoryDto categoryDto) {
        log.info("Добавление категории: {}", categoryDto.getName());
        return adminCategoryService.createCategory(categoryDto);
    }

    @DeleteMapping("/categories/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@Positive @PathVariable long catId) {
        log.info("Удаление категории: {}", catId);
        adminCategoryService.deleteCategory(catId);
    }

    @PatchMapping("/categories/{catId}")
    public CategoryDto updateCategory(@Positive @PathVariable long catId,
                                      @Valid @RequestBody CategoryDto categoryDto) {
        log.info("Обновление категории: {}", catId);
        return adminCategoryService.updateCategory(catId, categoryDto);
    }
}
