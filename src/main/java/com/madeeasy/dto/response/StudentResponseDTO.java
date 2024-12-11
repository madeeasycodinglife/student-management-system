package com.madeeasy.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StudentResponseDTO {
    private String name;
    private String email;
    private List<SubjectResponseDTO> subjects;
}
