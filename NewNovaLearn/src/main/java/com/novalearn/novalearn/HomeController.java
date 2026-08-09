package com.novalearn.novalearn;

import com.novalearn.novalearn.dto.CreateCourseForm;
import com.novalearn.novalearn.dto.ForgotPasswordForm;
import com.novalearn.novalearn.dto.RegisterForm;
import com.novalearn.novalearn.dto.ResetPasswordForm;
import com.novalearn.novalearn.model.Course;
import com.novalearn.novalearn.model.Role;
import com.novalearn.novalearn.model.User;
import com.novalearn.novalearn.service.CloudinaryService;
import com.novalearn.novalearn.service.CourseService;
import com.novalearn.novalearn.service.EmailService;
import com.novalearn.novalearn.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.security.core.context.SecurityContextHolder.clearContext;

@Controller
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    private final UserService userService;
    private final EmailService emailService;
    private final CourseService courseService;
    private final CloudinaryService cloudinaryService;
    private final PasswordEncoder passwordEncoder;
    private final com.novalearn.novalearn.repository.EnrollmentRepository enrollmentRepository;

    // Removed in-memory rate limiting as it is now database-backed

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public HomeController(UserService userService, EmailService emailService,
                          CourseService courseService, CloudinaryService cloudinaryService,
                          PasswordEncoder passwordEncoder,
                          com.novalearn.novalearn.repository.EnrollmentRepository enrollmentRepository) {
        this.userService = userService;
        this.emailService = emailService;
        this.courseService = courseService;
        this.cloudinaryService = cloudinaryService;
        this.passwordEncoder = passwordEncoder;
        this.enrollmentRepository = enrollmentRepository;
    }

    // ── Helper: resolve the current user from Principal ──────────────────────
    private Optional<User> currentUser(Principal principal) {
        if (principal == null) return Optional.empty();
        return userService.findByEmail(principal.getName());
    }

    // ── Helper: convert BindingResult field errors to a Map ──────────────────
    @SuppressWarnings("null")
    private Map<String, String> getFieldErrors(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing
                ));
    }

    // ── Public Pages ─────────────────────────────────────────────────────────

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String handleRegister(
            @Valid RegisterForm registerForm,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("fieldErrors", getFieldErrors(bindingResult));
            return "register";
        }
        try {
            userService.registerUser(registerForm.getFullName(), registerForm.getNickname(), registerForm.getEmail(), registerForm.getPassword(), registerForm.getRole());
            return "redirect:/login?success=Account+created+successfully!+Please+sign+in+to+continue.";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("fullName", registerForm.getFullName());
            model.addAttribute("nickname", registerForm.getNickname());
            model.addAttribute("email", registerForm.getEmail());
            return "register";
        }
    }

    @GetMapping("/login")
    public String login(
            @RequestParam(required = false) String success,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String selectedRole,
            Model model) {
        if (success != null && !success.isBlank()) model.addAttribute("successMessage", success);
        if (logout != null) model.addAttribute("successMessage", "You have been logged out successfully.");
        if ("role".equals(error)) {
            model.addAttribute("errorMessage", "The selected account type does not match this account.");
            model.addAttribute("email", email);
            model.addAttribute("selectedRole", selectedRole);
        } else if (error != null) {
            model.addAttribute("errorMessage", "Invalid email or password.");
        }
        return "login";
    }

    /**
     * Sends an authenticated user to the dashboard that matches the role stored
     * on their account. Navigation must never depend on a role passed in a URL.
     */
    @GetMapping("/dashboard")
    public String dashboard(Principal principal) {
        return currentUser(principal)
                .map(user -> user.getRole() == Role.LECTURER
                        ? "redirect:/lecturer-dashboard"
                        : "redirect:/student-dashboard")
                .orElse("redirect:/login");
    }



    // ── Browse Courses (public) ───────────────────────────────────────────────

    @GetMapping("/courses")
    public String courses(Principal principal, Model model, Pageable pageable) {
        Page<Course> coursePage = courseService.getAllCourses(pageable);
        model.addAttribute("allCourses", coursePage.getContent());
        model.addAttribute("coursePage", coursePage);
        currentUser(principal).ifPresent(u -> {
            model.addAttribute("nickname", u.getNickname());
            model.addAttribute("userRole", u.getRole().name());
        });
        return "courses";
    }

    // ── Enroll ───────────────────────────────────────────────────────────────

    @PostMapping("/enroll/{courseId}")
    @PreAuthorize("hasRole('STUDENT')")
    public String enrollInCourse(@PathVariable Long courseId, Principal principal) {
        Optional<Course> optCourse = courseService.findById(courseId);
        Optional<User> optUser = currentUser(principal);
        if (optCourse.isPresent() && optUser.isPresent()) {
            courseService.enrollStudent(optUser.get(), optCourse.get());
        }
        return "redirect:/course/" + courseId;
    }

    // ── Student Dashboard ─────────────────────────────────────────────────────

    @GetMapping("/student-dashboard")
    public String studentDashboard(
            @RequestParam(required = false) String success,
            @RequestParam(required = false) String deleteError,
            Principal principal,
            Model model) {
        model.addAttribute("role", "student");
        if (success != null) model.addAttribute("successMessage", success);
        if (deleteError != null) model.addAttribute("deleteError", deleteError);
        currentUser(principal).ifPresent(u -> {
            model.addAttribute("nickname", u.getNickname());
            model.addAttribute("fullName", u.getFullName());
            model.addAttribute("email", u.getEmail());
            model.addAttribute("joinedAt", u.getCreatedAt().toLocalDate().toString());
            List<Course> enrolled = courseService.getEnrolledCourses(u);
            model.addAttribute("enrolledCourses", enrolled);
            model.addAttribute("enrolledCount", enrolled.size());
            model.addAttribute("availableCourses", courseService.getAvailableCoursesForStudent(u));
        });
        model.addAttribute("allCourses", courseService.getAllCourses());
        return "dashboard";
    }

    // ── Lecturer Dashboard ────────────────────────────────────────────────────

    @GetMapping("/lecturer-dashboard")
    public String lecturerDashboard(
            @RequestParam(required = false) String success,
            @RequestParam(required = false) String deleteError,
            Principal principal,
            Model model) {
        model.addAttribute("role", "lecturer");
        if (success != null) model.addAttribute("successMessage", success);
        if (deleteError != null) model.addAttribute("deleteError", deleteError);
        currentUser(principal).ifPresent(u -> {
            model.addAttribute("nickname", u.getNickname());
            model.addAttribute("fullName", u.getFullName());
            model.addAttribute("email", u.getEmail());
            model.addAttribute("joinedAt", u.getCreatedAt().toLocalDate().toString());
            model.addAttribute("myCourses", courseService.getCoursesByLecturer(u));
        });
        return "dashboard";
    }

    // ── Create Course ─────────────────────────────────────────────────────────

    @GetMapping("/create-course")
    @PreAuthorize("hasRole('LECTURER')")
    public String showCreateCourse(Principal principal, Model model) {
        model.addAttribute("createCourseForm", new CreateCourseForm());
        currentUser(principal).ifPresent(u -> model.addAttribute("nickname", u.getNickname()));
        return "create-course";
    }

    @PostMapping("/create-course")
    @PreAuthorize("hasRole('LECTURER')")
    public String handleCreateCourse(
            @Valid CreateCourseForm form,
            BindingResult bindingResult,
            @RequestParam(required = false) MultipartFile videoFile,
            @RequestParam(required = false) MultipartFile documentFile,
            Principal principal,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("fieldErrors", getFieldErrors(bindingResult));
            model.addAttribute("createCourseForm", form);
            return "create-course";
        }

        Optional<User> optUser = currentUser(principal);
        if (optUser.isEmpty()) {
            return "redirect:/login";
        }

        User lecturer = optUser.get();
        model.addAttribute("nickname", lecturer.getNickname());

        try {
            if ("VIDEO".equals(form.getContentType())) {
                if (videoFile == null || videoFile.isEmpty()) {
                    model.addAttribute("errorMessage", "Please select a video file to upload.");
                    model.addAttribute("createCourseForm", form);
                    return "create-course";
                }
                String savedPath = saveUploadedFile(videoFile);
                courseService.createCourseVideo(form.getTitle(), form.getCategory(), form.getLevel(), form.getDescription(), savedPath, lecturer);
            } else {
                String documentPath = null;
                if (documentFile != null && !documentFile.isEmpty()) {
                    documentPath = saveUploadedFile(documentFile);
                }
                if ((form.getTextContent() == null || form.getTextContent().isBlank()) && documentPath == null) {
                    model.addAttribute("errorMessage", "Please enter some text content or upload a document for the course.");
                    model.addAttribute("createCourseForm", form);
                    return "create-course";
                }
                courseService.createCourseText(form.getTitle(), form.getCategory(), form.getLevel(), form.getDescription(), form.getTextContent(), documentPath, lecturer);
            }
            return "redirect:/lecturer-dashboard?success=Course+created+successfully!";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to create course: " + e.getMessage());
            model.addAttribute("createCourseForm", form);
            return "create-course";
        }
    }

    // ── View Course Detail ────────────────────────────────────────────────────

    @GetMapping("/course/{id}")
    public String viewCourse(@PathVariable Long id, Principal principal, Model model) {
        Optional<Course> optCourse = courseService.findById(id);
        if (optCourse.isEmpty()) return "redirect:/courses";

        Course course = optCourse.get();
        model.addAttribute("course", course);
        model.addAttribute("posts", courseService.getPostsForCourse(id));
        model.addAttribute("studentCount", courseService.countStudentsEnrolled(course));

        Optional<User> optUser = currentUser(principal);
        if (optUser.isPresent()) {
            User u = optUser.get();
            model.addAttribute("nickname", u.getNickname());
            model.addAttribute("userRole", u.getRole().name());

            boolean isOwner = course.getLecturer() != null &&
                    u.getEmail().equals(course.getLecturer().getEmail());
            boolean isLecturer = u.getRole() == com.novalearn.novalearn.model.Role.LECTURER;
            model.addAttribute("isOwner", isOwner);
            model.addAttribute("isLecturer", isLecturer);
            model.addAttribute("role", isOwner ? "lecturer" : (isLecturer ? "lecturer" : "student"));

            boolean isEnrolled = !isOwner && courseService.isEnrolled(u, course);
            model.addAttribute("isEnrolled", isEnrolled);
        } else {
            model.addAttribute("isOwner", false);
            model.addAttribute("isEnrolled", false);
            model.addAttribute("role", "guest");
        }

        return "course-detail";
    }

    // ── Add Topic Post ────────────────────────────────────────────────────────

    @PostMapping("/course/{id}/post")
    @PreAuthorize("hasRole('LECTURER')")
    public String addCoursePost(@PathVariable Long id,
                                @RequestParam String topic,
                                @RequestParam String contentType,
                                @RequestParam(required = false) String textContent,
                                @RequestParam(required = false) MultipartFile videoFile,
                                @RequestParam(required = false) MultipartFile documentFile,
                                Principal principal,
                                Model model) {

        Optional<Course> optCourse = courseService.findById(id);
        if (optCourse.isEmpty()) return "redirect:/lecturer-dashboard";
        Course course = optCourse.get();

        Optional<User> optUser = currentUser(principal);
        if (optUser.isEmpty()) return "redirect:/login";

        // Authorization check: only the course owner can add posts
        if (course.getLecturer() == null || !course.getLecturer().getEmail().equals(optUser.get().getEmail())) {
            return "redirect:/course/" + id;
        }

        model.addAttribute("nickname", optUser.get().getNickname());

        try {
            if ("VIDEO".equals(contentType)) {
                if (videoFile == null || videoFile.isEmpty()) {
                    model.addAttribute("errorMessage", "Please select a video file to upload.");
                    populateCourseModel(model, course, id, true);
                    return "course-detail";
                }
                String savedPath = saveUploadedFile(videoFile);
                courseService.addVideoPost(course, topic, savedPath);
            } else {
                String documentPath = null;
                if (documentFile != null && !documentFile.isEmpty()) {
                    documentPath = saveUploadedFile(documentFile);
                }
                if ((textContent == null || textContent.isBlank()) && documentPath == null) {
                    model.addAttribute("errorMessage", "Please enter some text content or upload a document for this topic.");
                    populateCourseModel(model, course, id, true);
                    return "course-detail";
                }
                courseService.addTextPost(course, topic, textContent, documentPath);
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to publish topic: " + e.getMessage());
            populateCourseModel(model, course, id, true);
            return "course-detail";
        }
        return "redirect:/course/" + id;
    }

    private void populateCourseModel(Model model, Course course, Long id, boolean isOwner) {
        model.addAttribute("course", course);
        model.addAttribute("posts", courseService.getPostsForCourse(id));
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("isEnrolled", false);
        model.addAttribute("studentCount", courseService.countStudentsEnrolled(course));
        model.addAttribute("role", "lecturer");
    }

    // ── File Upload Helper ────────────────────────────────────────────────────

    private String saveUploadedFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
        String ext = "";
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.')).toLowerCase();
        }

        // Validate file extensions to prevent malicious uploads (XSS/RCE)
        List<String> allowedExtensions = List.of(".mp4", ".webm", ".ogg", ".pdf", ".jpg", ".jpeg", ".png");
        if (!ext.isEmpty() && !allowedExtensions.contains(ext)) {
            throw new IllegalArgumentException("Invalid file type. Only video, PDF, and image files are allowed.");
        }

        String filename = UUID.randomUUID() + ext;
        Path tempFile = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);
        
        try {
            // Upload to Cloudinary
            return cloudinaryService.uploadFile(tempFile.toFile(), file.getContentType());
        } finally {
            // Delete temporary local file
            Files.deleteIfExists(tempFile);
        }
    }

    // ── Forgot / Reset Password ───────────────────────────────────────────────

    @GetMapping("/forgot-password")
    public String forgotPassword(Model model) {
        model.addAttribute("forgotPasswordForm", new ForgotPasswordForm());
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(
            @Valid ForgotPasswordForm form,
            BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("fieldErrors", getFieldErrors(bindingResult));
            return "forgot-password";
        }
        Optional<User> optionalUser = userService.findByEmail(form.getEmail().trim());
        
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            
            // Database-backed rate limiting check
            if (user.getPasswordResetLockedUntil() != null && user.getPasswordResetLockedUntil().isAfter(LocalDateTime.now())) {
                model.addAttribute("errorMessage", "Too many requests. Please wait 15 minutes before trying again.");
                return "forgot-password";
            }
            
            // Secure 6-digit token generation
            String token = String.format("%06d", new java.security.SecureRandom().nextInt(1000000));
            
            user.setResetCode(passwordEncoder.encode(token)); // Hash the token before storing
            user.setResetCodeExpiry(LocalDateTime.now().plusMinutes(10));
            user.setPasswordResetAttempts(user.getPasswordResetAttempts() + 1);
            
            if (user.getPasswordResetAttempts() >= 3) {
                user.setPasswordResetLockedUntil(LocalDateTime.now().plusMinutes(15));
            }
            userService.save(user);

            try {
                emailService.sendPasswordResetEmail(form.getEmail(), token);
            } catch (Exception e) {
                log.error("Email error: {}", e.getMessage());
                model.addAttribute("errorMessage", "Failed to send email. Check your SMTP configuration in .env.");
                return "forgot-password";
            }
        }

        // Generic success message to prevent user enumeration
        model.addAttribute("successMessage", "If the email is registered, a 6-digit code has been sent. Check your inbox.");
        model.addAttribute("codeSent", true);
        model.addAttribute("email", form.getEmail());
        return "forgot-password";
    }

    @PostMapping("/verify-code")
    public String verifyCode(@RequestParam String email, @RequestParam String code, Model model) {
        Optional<User> optionalUser = userService.findByEmail(email.trim());
        if (optionalUser.isPresent() && optionalUser.get().getResetCode() != null 
                && passwordEncoder.matches(code, optionalUser.get().getResetCode())) {
            
            if (optionalUser.get().getResetCodeExpiry() == null ||
                    optionalUser.get().getResetCodeExpiry().isBefore(LocalDateTime.now())) {
                model.addAttribute("errorMessage", "This code has expired. Please request a new one.");
                model.addAttribute("codeSent", true);
                model.addAttribute("email", email);
                return "forgot-password";
            }
            // Pass email and plain code combined to avoid front-end changes
            model.addAttribute("token", email.trim() + ":" + code);
            return "reset-password";
        }
        model.addAttribute("errorMessage", "Invalid reset code. Please try again.");
        model.addAttribute("codeSent", true);
        model.addAttribute("email", email);
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetForm(Model model) {
        model.addAttribute("resetPasswordForm", new ResetPasswordForm());
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(
            @Valid ResetPasswordForm form,
            BindingResult bindingResult,
            @RequestParam String token,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("fieldErrors", getFieldErrors(bindingResult));
            model.addAttribute("token", token);
            return "reset-password";
        }
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            model.addAttribute("errorMessage", "Passwords do not match.");
            model.addAttribute("token", token);
            return "reset-password";
        }
        if (form.getPassword().length() < 8) {
            model.addAttribute("errorMessage", "Password must be at least 8 characters.");
            model.addAttribute("token", token);
            return "reset-password";
        }

        String[] parts = token.split(":");
        if (parts.length != 2) {
            model.addAttribute("errorMessage", "Invalid token format.");
            return "reset-password";
        }
        String email = parts[0];
        String plainCode = parts[1];

        Optional<User> optionalUser = userService.findByEmail(email);
        if (optionalUser.isEmpty() || optionalUser.get().getResetCodeExpiry() == null || 
                optionalUser.get().getResetCodeExpiry().isBefore(LocalDateTime.now()) || 
                !passwordEncoder.matches(plainCode, optionalUser.get().getResetCode())) {
            model.addAttribute("errorMessage", "This code is invalid or has expired. Please request a new one.");
            return "reset-password";
        }

        User user = optionalUser.get();
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setResetCode(null);
        user.setResetCodeExpiry(null);
        user.setPasswordResetAttempts(0);
        user.setPasswordResetLockedUntil(null);
        userService.save(user);

        return "redirect:/login?success=Password+reset+successfully.+You+can+now+sign+in.";
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login?logout=true";
    }

    // ── Delete Course (owner only) ───────────────────────────────────────

    @PostMapping("/course/{id}/delete")
    @PreAuthorize("hasRole('LECTURER')")
    public String deleteCourse(@PathVariable Long id, Principal principal) {
        currentUser(principal).ifPresent(u -> courseService.deleteCourse(id, u));
        return "redirect:/lecturer-dashboard?success=Course+deleted+successfully";
    }

    // ── Delete Post (course owner only) ───────────────────────────────

    @PostMapping("/post/{id}/delete")
    @PreAuthorize("hasRole('LECTURER')")
    public String deletePost(@PathVariable Long id, @RequestParam Long courseId, Principal principal) {
        currentUser(principal).ifPresent(u -> courseService.deletePost(id, u));
        return "redirect:/course/" + courseId;
    }

    // ── Edit Post topic/text (course owner only) ────────────────────────

    @PostMapping("/post/{id}/edit")
    @PreAuthorize("hasRole('LECTURER')")
    public String editPost(@PathVariable Long id,
                           @RequestParam Long courseId,
                           @RequestParam(required = false) String topic,
                           @RequestParam(required = false) String textContent,
                           Principal principal) {
        currentUser(principal).ifPresent(u -> courseService.editPost(id, topic, textContent, u));
        return "redirect:/course/" + courseId;
    }

    // ── Delete Account (password-verified) ────────────────────────────

    @PostMapping("/delete-account")
    public String deleteAccount(@RequestParam String confirmPassword,
                                Principal principal,
                                HttpServletRequest request) {
        Optional<User> optUser = currentUser(principal);
        if (optUser.isPresent()) {
            User u = optUser.get();
            if (passwordEncoder.matches(confirmPassword, u.getPassword())) {
                // Detach lecturer courses (keep them in DB) and remove student enrollments
                enrollmentRepository.deleteByStudent(u);
                courseService.detachUserCourses(u);
                userService.deleteUser(u);
                // Invalidate session
                HttpSession session = request.getSession(false);
                if (session != null) session.invalidate();
                org.springframework.security.core.context.SecurityContextHolder.clearContext();
                return "redirect:/login?accountDeleted=true";
            } else {
                // Wrong password — redirect back to whichever dashboard with error
                String role = u.getRole().name();
                String dash = role.equals("LECTURER") ? "/lecturer-dashboard" : "/student-dashboard";
                return "redirect:" + dash + "?deleteError=Incorrect+password";
            }
        }
        return "redirect:/login";
    }
}
