package com.madeeasy.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubjectRequestDTO {
    @NotBlank(message = "name cannot be blank")
    private String name;
    @NotBlank(message = "instructor cannot be blank")
    private String instructor;
    @NotBlank(message = "semester cannot be blank")
    private String semester;
}
