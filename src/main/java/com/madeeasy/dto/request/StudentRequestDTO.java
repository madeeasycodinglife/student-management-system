package com.madeeasy.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class StudentRequestDTO {
    private String email;
    private List<String> subjectIds;
}
