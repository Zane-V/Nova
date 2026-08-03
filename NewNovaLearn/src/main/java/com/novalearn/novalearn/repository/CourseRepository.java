package com.novalearn.novalearn.repository;

import com.novalearn.novalearn.model.Course;
import com.novalearn.novalearn.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    @EntityGraph(attributePaths = {"lecturer"})
    Optional<Course> findById(Long id);

    List<Course> findByLecturer(User lecturer);
    List<Course> findByCategory(String category);
    Page<Course> findAll(Pageable pageable);
}
