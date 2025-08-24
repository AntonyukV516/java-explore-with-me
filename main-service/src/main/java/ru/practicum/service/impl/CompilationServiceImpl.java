package ru.practicum.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.model.dto.RequestCompilationDto;
import ru.practicum.model.dto.ResponseCompilationDto;
import ru.practicum.model.dto.UpdateCompilationRequest;
import ru.practicum.model.entity.Compilation;
import ru.practicum.model.entity.Event;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.CompilationService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Override
    public ResponseCompilationDto saveCompilation(RequestCompilationDto requestCompilationDto) {
        Compilation compilation = compilationMapper.toEntity(requestCompilationDto);

        if (requestCompilationDto.getEvents() != null && !requestCompilationDto.getEvents().isEmpty()) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(requestCompilationDto.getEvents()));
            compilation.setEvents(events);
        }

        Compilation savedCompilation = compilationRepository.save(compilation);
        return compilationMapper.toDto(savedCompilation);
    }

    @Override
    public void deleteCompilation(Long compId) {
        checkCompilationExists(compId);
        compilationRepository.deleteById(compId);
    }

    @Override
    public ResponseCompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateRequest) {
        Compilation compilation = getCompilationById(compId);

        if (updateRequest.getTitle() != null) {
            compilation.setTitle(updateRequest.getTitle());
        }

        if (updateRequest.getPinned() != null) {
            compilation.setPinned(updateRequest.getPinned());
        }

        if (updateRequest.getEvents() != null) {
            Set<Event> events = new HashSet<>(eventRepository.findAllById(updateRequest.getEvents()));
            compilation.setEvents(events);
        }

        Compilation updatedCompilation = compilationRepository.save(compilation);
        return compilationMapper.toDto(updatedCompilation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResponseCompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);

        if (pinned != null) {
            return compilationRepository.findAllByPinned(pinned, pageRequest).stream()
                    .map(compilationMapper::toDto)
                    .collect(Collectors.toList());
        }

        return compilationRepository.findAll(pageRequest).stream()
                .map(compilationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseCompilationDto getCompilation(Long compId) {
        Compilation compilation = getCompilationById(compId);
        return compilationMapper.toDto(compilation);
    }

    private Compilation getCompilationById(Long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException("Compilation with id=" + compId + " was not found"));
    }

    private void checkCompilationExists(Long compId) {
        if (!compilationRepository.existsById(compId)) {
            throw new NotFoundException("Compilation with id=" + compId + " was not found");
        }
    }
}