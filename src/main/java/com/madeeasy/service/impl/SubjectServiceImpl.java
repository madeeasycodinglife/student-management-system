package com.madeeasy.service.impl;

import com.madeeasy.dto.request.SubjectRequestDTO;
import com.madeeasy.dto.response.SubjectResponseDTO;
import com.madeeasy.entity.Subject;
import com.madeeasy.repository.SubjectRepository;
import com.madeeasy.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;

    @Override
    public SubjectResponseDTO createSubject(SubjectRequestDTO subjectRequestDTO) {
        Subject subject = Subject.builder()
                .id(UUID.randomUUID().toString())
                .name(subjectRequestDTO.getName())
                .semester(subjectRequestDTO.getSemester())
                .instructor(subjectRequestDTO.getInstructor())
                .build();
        Subject savedSubject = this.subjectRepository.save(subject);

        return SubjectResponseDTO.builder()
                .id(savedSubject.getId())
                .name(savedSubject.getName())
                .semester(savedSubject.getSemester())
                .instructor(savedSubject.getInstructor())
                .build();
    }

    @Override
    public List<SubjectResponseDTO> getAllSubjects() {

        List<Subject> subjectList = this.subjectRepository.findAll();
        if (subjectList.isEmpty()) {
            return Collections.emptyList();
        }
        return subjectList.stream()
                .map(subject -> SubjectResponseDTO.builder()
                        .id(subject.getId())
                        .name(subject.getName())
                        .semester(subject.getSemester())
                        .instructor(subject.getInstructor())
                        .build())
                .toList();
    }
}
