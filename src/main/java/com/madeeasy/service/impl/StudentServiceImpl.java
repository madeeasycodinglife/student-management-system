package com.madeeasy.service.impl;

import com.madeeasy.dto.request.StudentRequestDTO;
import com.madeeasy.dto.response.StudentResponseDTO;
import com.madeeasy.dto.response.SubjectResponseDTO;
import com.madeeasy.entity.Student;
import com.madeeasy.entity.Subject;
import com.madeeasy.entity.User;
import com.madeeasy.exception.SubjectNotFoundException;
import com.madeeasy.exception.UsernameNotFoundException;
import com.madeeasy.repository.StudentRepository;
import com.madeeasy.repository.SubjectRepository;
import com.madeeasy.repository.UserRepository;
import com.madeeasy.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;

    @Override
    public StudentResponseDTO createStudent(StudentRequestDTO studentRequestDTO) {
        // Fetch subjects by IDs
        List<Subject> subjectsByIds = this.subjectRepository.findSubjectsByIds(studentRequestDTO.getSubjectIds());

        // Fetch user by email
        User user = this.userRepository.findByEmail(studentRequestDTO.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Email Not Found !"));

        // Ensure the subjects are valid
        if (subjectsByIds.isEmpty()) {
            throw new SubjectNotFoundException("No subjects found for the provided subject IDs.");
        }

        // Check if the student already exists
        Student student = this.studentRepository.findByEmail(studentRequestDTO.getEmail())
                .orElse(null);

        if (student == null) {
            // If student doesn't exist, create a new one
            student = Student.builder()
                    .id(UUID.randomUUID().toString())
                    .fullName(user.getFullName())
                    .email(studentRequestDTO.getEmail())
                    .userId(user.getId())
                    .subjects(subjectsByIds) // Associate subjects
                    .build();

            // Save the student
            Student savedStudent = this.studentRepository.save(student);

            // After saving the student, update the subjects' student list
            for (Subject subject : savedStudent.getSubjects()) {
                subject.getStudents().add(savedStudent); // Ensure the reverse relationship is updated
                this.subjectRepository.save(subject); // Persist the updated subject
            }

            // Return the student response with subjects
            return StudentResponseDTO.builder()
                    .name(user.getFullName())
                    .email(savedStudent.getEmail())
                    .subjects(savedStudent.getSubjects().stream()
                            .map(subject -> SubjectResponseDTO.builder()
                                    .id(subject.getId())
                                    .name(subject.getName())
                                    .semester(subject.getSemester())
                                    .instructor(subject.getInstructor())
                                    .build())
                            .collect(Collectors.toList())
                    )
                    .build();
        } else {
            // If student already exists, check for existing subjects and add new ones
            List<Subject> existingSubjects = student.getSubjects();

            // Filter out subjects that are already assigned to the student
            List<Subject> newSubjects = subjectsByIds.stream()
                    .filter(subject -> existingSubjects.stream()
                            .noneMatch(existingSubject -> existingSubject.getId().equals(subject.getId())))
                    .collect(Collectors.toList());

            // Add the new subjects that don't exist yet
            existingSubjects.addAll(newSubjects);
            student.setSubjects(existingSubjects);

            // Save the updated student (this will also update the join table if necessary)
            Student savedStudent = this.studentRepository.save(student);
            // After saving the student, ensure that each subject also knows about this student
            for (Subject subject : savedStudent.getSubjects()) {
                if (!subject.getStudents().contains(savedStudent)) {
                    subject.getStudents().add(savedStudent); // Ensure the reverse relationship is updated
                    this.subjectRepository.save(subject); // Persist the updated subject
                }
            }
            // Return the updated student response with subjects
            return StudentResponseDTO.builder()
                    .name(user.getFullName())
                    .email(savedStudent.getEmail())
                    .subjects(savedStudent.getSubjects().stream()
                            .map(subject -> SubjectResponseDTO.builder()
                                    .id(subject.getId())
                                    .name(subject.getName())
                                    .semester(subject.getSemester())
                                    .instructor(subject.getInstructor())
                                    .build())
                            .collect(Collectors.toList())
                    )
                    .build();
        }
    }

    @Override
    public List<StudentResponseDTO> getAllStudents() {
        List<Student> studentList = this.studentRepository.findAll();
        System.out.println("studentList = " + studentList);
        if (studentList.isEmpty()) {
            return Collections.emptyList();
        }
        return studentList.stream()
                .map(student -> StudentResponseDTO.builder()
                        .name(student.getFullName())
                        .email(student.getEmail())
                        .subjects(student.getSubjects().stream()
                                .map(subject -> SubjectResponseDTO.builder()
                                        .id(subject.getId())
                                        .name(subject.getName())
                                        .semester(subject.getSemester())
                                        .instructor(subject.getInstructor())
                                        .build())
                                .toList()
                        )
                        .build())
                .toList();
    }
}
