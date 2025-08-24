package ru.practicum.controller.adminControllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.model.dto.RequestCategoryDto;
import ru.practicum.model.dto.ResponseCategoryDto;
import ru.practicum.service.CategoryService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseCategoryDto addCategory(@Valid @RequestBody RequestCategoryDto requestCategoryDto) {
        return categoryService.addCategory(requestCategoryDto);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        categoryService.deleteCategory(catId);
    }

    @PatchMapping("/{catId}")
    public ResponseCategoryDto updateCategory(@PathVariable Long catId,
                                              @Valid @RequestBody RequestCategoryDto categoryDto) {
        return categoryService.updateCategory(catId, categoryDto);
    }
}
