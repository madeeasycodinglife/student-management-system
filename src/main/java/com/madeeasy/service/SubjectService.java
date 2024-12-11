package com.madeeasy.service;

import com.madeeasy.dto.request.SubjectRequestDTO;
import com.madeeasy.dto.response.SubjectResponseDTO;

import java.util.List;

public interface SubjectService {
    SubjectResponseDTO createSubject(SubjectRequestDTO subjectRequestDTO);

    List<SubjectResponseDTO> getAllSubjects();
}
