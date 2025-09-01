package ru.practicum.service;

import ru.practicum.model.dto.category.CategoryDto;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> getAll(Integer from, Integer size);

    CategoryDto getById(Long id);

    CategoryDto create(CategoryDto newCategoryDto);

    CategoryDto update(Long id, CategoryDto newCategoryDto);

    void delete(Long id);
}