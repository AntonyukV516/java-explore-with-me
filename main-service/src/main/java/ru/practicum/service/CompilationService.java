package ru.practicum.service;

import ru.practicum.model.dto.RequestCompilationDto;
import ru.practicum.model.dto.ResponseCompilationDto;
import ru.practicum.model.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    ResponseCompilationDto saveCompilation(RequestCompilationDto requestCompilationDto);
    void deleteCompilation(Long compId);
    ResponseCompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest);
    List<ResponseCompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);
    ResponseCompilationDto getCompilation(Long compId);
}
