package ru.practicum.service;

import ru.practicum.model.dto.RequestCategoryDto;
import ru.practicum.model.dto.ResponseCategoryDto;

import java.util.List;

public interface CategoryService {
    ResponseCategoryDto addCategory(RequestCategoryDto requestCategoryDto);
    void deleteCategory(Long catId);
    ResponseCategoryDto updateCategory(Long catId, RequestCategoryDto requestCategoryDto);
    List<ResponseCategoryDto> getCategories(Integer from, Integer size);
    ResponseCategoryDto getCategory(Long catId);
}