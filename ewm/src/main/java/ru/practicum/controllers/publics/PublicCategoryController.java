package ru.practicum.controllers.publics;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CategoryDto;
import ru.practicum.services.publics.PublicCategoryService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/categories")
@AllArgsConstructor
@Slf4j
public class PublicCategoryController {
    private PublicCategoryService publicCategoryService;

    @GetMapping()
    public List<CategoryDto> getCategories(@PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                           @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("Получение списка категорий");
        return publicCategoryService.getCategories(from, size);
    }

    @GetMapping("/{catId}")
    public CategoryDto getCategory(@Positive @PathVariable long catId) {
        log.info("олучение категории id: {}", catId);
        return publicCategoryService.getCategory(catId);
    }
}
