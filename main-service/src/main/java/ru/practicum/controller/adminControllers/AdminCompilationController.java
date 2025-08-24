package ru.practicum.controller.adminControllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.model.dto.RequestCompilationDto;
import ru.practicum.model.dto.ResponseCompilationDto;
import ru.practicum.model.dto.UpdateCompilationRequest;
import ru.practicum.service.CompilationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/compilations")
public class AdminCompilationController {

    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseCompilationDto saveCompilation(@Valid @RequestBody RequestCompilationDto requestCompilationDto) {
        return compilationService.saveCompilation(requestCompilationDto);
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        compilationService.deleteCompilation(compId);
    }

    @PatchMapping("/{compId}")
    public ResponseCompilationDto updateCompilation(@PathVariable Long compId,
                                            @RequestBody UpdateCompilationRequest updateRequest) {
        return compilationService.updateCompilation(compId, updateRequest);
    }
}
