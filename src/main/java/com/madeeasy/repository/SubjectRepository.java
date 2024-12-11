package com.madeeasy.repository;

import com.madeeasy.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, String> {
    @Query("SELECT s FROM Subject s WHERE s.id IN :ids")
    List<Subject> findSubjectsByIds(List<String> ids);
}
