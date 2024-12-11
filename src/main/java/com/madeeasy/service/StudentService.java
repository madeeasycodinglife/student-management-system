package com.madeeasy.service;

import com.madeeasy.dto.request.StudentRequestDTO;
import com.madeeasy.dto.response.StudentResponseDTO;

import java.util.List;

public interface StudentService {

    StudentResponseDTO createStudent(StudentRequestDTO studentDTO);

    List <StudentResponseDTO> getAllStudents();
}
