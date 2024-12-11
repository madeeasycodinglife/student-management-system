package com.madeeasy.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubjectResponseDTO {
    private String id;
    private String name;
    private String instructor;
    private String semester;
}
