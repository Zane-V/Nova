package com.novalearn.novalearn.service;

import com.novalearn.novalearn.model.Course;
import com.novalearn.novalearn.model.CoursePost;
import com.novalearn.novalearn.model.Enrollment;
import com.novalearn.novalearn.model.User;
import com.novalearn.novalearn.repository.CoursePostRepository;
import com.novalearn.novalearn.repository.CourseRepository;
import com.novalearn.novalearn.repository.EnrollmentRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final CoursePostRepository coursePostRepository;
    private final EnrollmentRepository enrollmentRepository;

    public CourseService(CourseRepository courseRepository,
                         CoursePostRepository coursePostRepository,
                         EnrollmentRepository enrollmentRepository) {
        this.courseRepository = courseRepository;
        this.coursePostRepository = coursePostRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    // ---- Course CRUD ----

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Cacheable(value = "courses", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Course> getAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable);
    }

    public List<Course> getCoursesByLecturer(User lecturer) {
        return courseRepository.findByLecturer(lecturer);
    }

    public Optional<Course> findById(Long id) {
        return courseRepository.findById(id);
    }

    /** Create a video-based course */
    public Course createCourseVideo(String title, String category, String level,
                                    String description, String videoPath, User lecturer) {
        Course course = Course.builder()
                .title(title).category(category).level(level)
                .description(description).contentType("VIDEO")
                .videoPath(videoPath).lecturer(lecturer)
                .build();
        return courseRepository.save(course);
    }

    /** Create a text-based course */
    public Course createCourseText(String title, String category, String level,
                                   String description, String textContent, String documentPath, User lecturer) {
        Course course = Course.builder()
                .title(title).category(category).level(level)
                .description(description).contentType("TEXT")
                .textContent(textContent).documentPath(documentPath).lecturer(lecturer)
                .build();
        return courseRepository.save(course);
    }

    // ---- Enrollment ----

    /**
     * Enroll a student in a course. Silently ignores duplicate enrollments.
     */
    public Enrollment enrollStudent(User student, Course course) {
        if (enrollmentRepository.existsByStudentAndCourse(student, course)) {
            return enrollmentRepository.findByStudentAndCourse(student, course).orElseThrow();
        }
        Enrollment e = Enrollment.builder().student(student).course(course).build();
        return enrollmentRepository.save(e);
    }

    /** All courses a student is enrolled in, newest first */
    public List<Course> getEnrolledCourses(User student) {
        return enrollmentRepository.findByStudentOrderByEnrolledAtDesc(student)
                .stream()
                .map(e -> e.getCourse())
                .collect(Collectors.toList());
    }

    /** Courses a student can still discover and enroll in. */
    public List<Course> getAvailableCoursesForStudent(User student) {
        Set<Long> enrolledCourseIds = enrollmentRepository.findByStudentOrderByEnrolledAtDesc(student)
                .stream()
                .map(enrollment -> enrollment.getCourse().getId())
                .collect(Collectors.toSet());

        return courseRepository.findAll().stream()
                .filter(course -> !enrolledCourseIds.contains(course.getId()))
                .collect(Collectors.toList());
    }

    /** How many courses a student is enrolled in */
    public long countEnrollments(User student) {
        return enrollmentRepository.countByStudent(student);
    }

    /** Whether a student is enrolled in a specific course */
    public boolean isEnrolled(User student, Course course) {
        return enrollmentRepository.existsByStudentAndCourse(student, course);
    }

    /** How many students enrolled in a course */
    public long countStudentsEnrolled(Course course) {
        return enrollmentRepository.countByCourse(course);
    }

    // ---- Topic Posts ----

    public List<CoursePost> getPostsForCourse(Long courseId) {
        return coursePostRepository.findByCourseIdOrderByCreatedAtAsc(courseId);
    }

    public CoursePost addVideoPost(Course course, String topic, String videoPath) {
        CoursePost post = CoursePost.builder()
                .course(course).topic(topic)
                .contentType("VIDEO").videoPath(videoPath)
                .build();
        return coursePostRepository.save(post);
    }

    public CoursePost addTextPost(Course course, String topic, String textContent, String documentPath) {
        CoursePost post = CoursePost.builder()
                .course(course).topic(topic)
                .contentType("TEXT").textContent(textContent).documentPath(documentPath)
                .build();
        return coursePostRepository.save(post);
    }

    @Transactional
    public void deleteCourse(Long courseId, User requester) {
        Course course = courseRepository.findById(courseId).orElseThrow();
        if (course.getLecturer() != null && course.getLecturer().getId().equals(requester.getId())) {
            enrollmentRepository.deleteByCourse(course);
            coursePostRepository.deleteByCourse(course);
            courseRepository.delete(course);
        }
    }

    @Transactional
    public void deletePost(Long postId, User requester) {
        CoursePost post = coursePostRepository.findById(postId).orElseThrow();
        if (post.getCourse().getLecturer() != null && post.getCourse().getLecturer().getId().equals(requester.getId())) {
            coursePostRepository.delete(post);
        }
    }

    @Transactional
    public CoursePost editPost(Long postId, String topic, String textContent, User requester) {
        CoursePost post = coursePostRepository.findById(postId).orElseThrow();
        if (post.getCourse().getLecturer() != null && post.getCourse().getLecturer().getId().equals(requester.getId())) {
            if (topic != null) post.setTopic(topic);
            if (textContent != null) post.setTextContent(textContent);
            return coursePostRepository.save(post);
        }
        return post;
    }

    @Transactional
    public void detachUserCourses(User user) {
        List<Course> courses = courseRepository.findByLecturer(user);
        for (Course c : courses) {
            c.setLecturer(null);
            courseRepository.save(c);
        }
    }
}
