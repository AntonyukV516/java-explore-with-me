package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.model.dto.RequestCompilationDto;
import ru.practicum.model.dto.ResponseCompilationDto;
import ru.practicum.model.entity.Compilation;

@Mapper(componentModel = "spring", uses = {EventMapper.class})
public interface CompilationMapper {

    @Mapping(target = "events", ignore = true)
    Compilation toEntity(RequestCompilationDto newCompilationDto);

    ResponseCompilationDto toDto(Compilation compilation);
}
