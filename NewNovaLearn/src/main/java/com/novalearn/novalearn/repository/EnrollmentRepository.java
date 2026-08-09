package com.novalearn.novalearn.repository;

import com.novalearn.novalearn.model.Course;
import com.novalearn.novalearn.model.Enrollment;
import com.novalearn.novalearn.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    @EntityGraph(attributePaths = {"course"})
    List<Enrollment> findByStudentOrderByEnrolledAtDesc(User student);

    Optional<Enrollment> findByStudentAndCourse(User student, Course course);

    boolean existsByStudentAndCourse(User student, Course course);

    long countByCourse(Course course);

    long countByStudent(User student);
    void deleteByStudent(User student);
    void deleteByCourse(Course course);
}
