package com.madeeasy.controller;

import com.madeeasy.dto.request.StudentRequestDTO;
import com.madeeasy.dto.response.StudentResponseDTO;
import com.madeeasy.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/student-service")
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    public ResponseEntity<?> createStudent(@RequestBody StudentRequestDTO studentDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.createStudent(studentDTO));
    }

    @GetMapping
    public ResponseEntity<?> getAllStudents() {
        List<StudentResponseDTO> allStudents = this.studentService.getAllStudents();
        if (allStudents.isEmpty()) {
            ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        return ResponseEntity.ok(studentService.getAllStudents());
    }
}
