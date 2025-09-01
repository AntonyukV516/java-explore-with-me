package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.dto.category.CategoryDto;
import ru.practicum.model.entity.Category;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.CategoryService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    public List<CategoryDto> getAll(Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return categoryRepository.findAll(pageable)
                .getContent()
                .stream()
                .map(CategoryMapper::categoryToDto)
                .toList();
    }

    @Override
    public CategoryDto getById(Long id) {
        return CategoryMapper.categoryToDto(
                categoryRepository.findById(id).orElseThrow(
                        () -> new NotFoundException("Категория с id=" + id + " не найдена")
                )
        );
    }

    @Override
    public CategoryDto create(CategoryDto newCategoryDto) {
        if (!categoryRepository.findByNameIgnoreCase(newCategoryDto.getName()).isEmpty()) {
            throw new ConditionsNotMetException("Категория с именем " + newCategoryDto.getName() + " уже существует");
        }

        return CategoryMapper.categoryToDto(
                categoryRepository.saveAndFlush(CategoryMapper.requestToCategory(newCategoryDto))
        );
    }

    @Override
    public CategoryDto update(Long id, CategoryDto newCategoryDto) {
        Category category = categoryRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Категория с id=" + id + " не найдена")
        );
        List<Category> categories = categoryRepository.findByNameIgnoreCase(newCategoryDto.getName());
        if (!categories.isEmpty() && !categories.getFirst().getId().equals(id)) {
            throw new ConditionsNotMetException("Категория с именем " + newCategoryDto.getName() + " уже существует");
        }

        category.setName(newCategoryDto.getName());
        return CategoryMapper.categoryToDto(categoryRepository.saveAndFlush(category));
    }

    @Override
    public void delete(Long id) {
        categoryRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Категория с id=" + id + " не найдена")
        );
        if (!eventRepository.findAllByCategoryId(id).isEmpty()) {
            throw new ConditionsNotMetException("Удаление категории невозможно, так как она используется в событиях");
        }
        categoryRepository.deleteById(id);
    }
}