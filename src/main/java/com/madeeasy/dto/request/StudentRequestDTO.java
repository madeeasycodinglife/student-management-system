package com.madeeasy.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class StudentRequestDTO {
    @NotBlank(message = "Full name is required.")
    @Email(message = "email should be valid")
    private String email;
    @NotEmpty(message = "roles cannot be empty")
    private List<@NotBlank(message = "role cannot be blank") String> subjectIds;
}
