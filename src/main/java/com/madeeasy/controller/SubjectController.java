package com.madeeasy.controller;

import com.madeeasy.dto.request.SubjectRequestDTO;
import com.madeeasy.dto.response.SubjectResponseDTO;
import com.madeeasy.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/subject-service")
public class SubjectController {

    private final SubjectService subjectService;

    @GetMapping(path = "/all")
    public ResponseEntity<?> getAllSubjects() {
        List<SubjectResponseDTO> allSubjects = this.subjectService.getAllSubjects();
        if (allSubjects.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No subjects available");
        }
        return ResponseEntity.status(HttpStatus.OK).body(allSubjects);
    }

    @PostMapping(path = "/create")
    public ResponseEntity<?> createSubject(@Valid @RequestBody SubjectRequestDTO subjectRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.subjectService.createSubject(subjectRequestDTO));
    }
}
