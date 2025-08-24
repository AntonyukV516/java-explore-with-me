package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.dto.RequestCategoryDto;
import ru.practicum.model.dto.ResponseCategoryDto;
import ru.practicum.model.entity.Category;
import ru.practicum.model.entity.Event;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.CategoryService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public ResponseCategoryDto addCategory(RequestCategoryDto requestCategoryDto) {
        checkCategoryNameUnique(requestCategoryDto.getName());
        Category category = categoryMapper.toEntity(requestCategoryDto);
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toDto(savedCategory);
    }

    @Override
    public void deleteCategory(Long catId) {
        Category category = getCategoryById(catId);
        checkCategoryNotUsed(catId);
        categoryRepository.delete(category);
    }

    @Override
    public ResponseCategoryDto updateCategory(Long catId, RequestCategoryDto requestCategoryDto) {
        Category category = getCategoryById(catId);

        if (!category.getName().equals(requestCategoryDto.getName())) {
            checkCategoryNameUnique(requestCategoryDto.getName());
        }

        category.setName(requestCategoryDto.getName());
        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toDto(updatedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResponseCategoryDto> getCategories(Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageRequest).stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseCategoryDto getCategory(Long catId) {
        Category category = getCategoryById(catId);
        return categoryMapper.toDto(category);
    }

    private Category getCategoryById(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));
    }

    private void checkCategoryNameUnique(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new ConflictException("Category with name='" + name + "' already exists");
        }
    }

    private void checkCategoryNotUsed(Long catId) {
        List<Event> events = eventRepository.findAllByCategoryId(catId);
        if (!events.isEmpty()) {
            throw new ConflictException("The category is not empty");
        }
    }
}
