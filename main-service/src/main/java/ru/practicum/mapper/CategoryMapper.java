package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.model.dto.RequestCategoryDto;
import ru.practicum.model.dto.ResponseCategoryDto;
import ru.practicum.model.entity.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toEntity(RequestCategoryDto requestCategoryDto);

    ResponseCategoryDto toDto(Category category);
}
