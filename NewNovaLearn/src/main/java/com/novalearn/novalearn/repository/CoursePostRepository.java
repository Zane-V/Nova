package com.novalearn.novalearn.repository;

import com.novalearn.novalearn.model.CoursePost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoursePostRepository extends JpaRepository<CoursePost, Long> {
    List<CoursePost> findByCourseIdOrderByCreatedAtAsc(Long courseId);
}
