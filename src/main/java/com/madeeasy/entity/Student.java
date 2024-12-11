package com.madeeasy.entity;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    private String id;
    private String fullName;
    private String email;
    private String userId; // this will point to the specific user


    @ManyToMany(mappedBy = "students")
    private List<Subject> subjects;
}
