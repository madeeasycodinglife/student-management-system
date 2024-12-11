package com.madeeasy.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String pin;
    private String city;
    private String state;
    private String country;

    @OneToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
}
