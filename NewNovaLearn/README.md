# NovaLearn

**NovaLearn** is a modern, full-stack Learning Management System (LMS) built with Spring Boot and PostgreSQL. It bridges the gap between educators and students through an intuitive, interactive, and beautifully designed interface.

> **UI update:** The Home, Login, Register, Courses, and Dashboard pages have been restyled with the Bootstrap-based "Nova Learning" design (blue theme, `assets/css/style.css`) while keeping every backend binding (Thymeleaf forms, CSRF, role logic, course data) fully intact. A shared navbar (`templates/fragments/navbar.html`) is now used across pages and automatically shows Login/Register or Dashboard/Logout depending on whether you're signed in. The remaining pages (course detail, create course, create session, sessions, forgot/reset password, live session) got the same color palette and fonts applied but keep their original layouts — let me know if you'd like those fully redesigned to match too.
>
> **Security note:** the `.env` file that was in this project contained a live Gmail app password, so it was **not** included in this delivery — only `.env.example` was kept. Recreate your own `.env` locally with fresh credentials (and consider rotating that Gmail app password since it was shared in a project export).

##  Features

- **Role-Based Architecture:** Dedicated dashboards and permission sets for both Students and Lecturers.
- **Dynamic Course Creation:** Lecturers can effortlessly create and publish multi-format courses (Video or Text based).
- **Topic Updates:** Instructors can post ongoing updates, announcements, and supplementary content directly to enrolled students.
- **Secure Access Control:** Premium course content and topic updates are strictly locked behind an automated enrollment system.
- **Modern User Interface:** Built with Thymeleaf and Vanilla CSS, featuring a glassmorphism design system, smooth micro-animations, dynamic content toggles, and real-time password strength validation.
- **Robust Authentication:** Secure registration, login, and a custom multi-step "Forgot Password" flow utilizing 6-digit email verification codes.
- **Database Persistence:** Fully backed by PostgreSQL using Spring Data JPA, optimized with `@EntityGraph` for high-performance relationship fetching.

##  Technology Stack

- **Backend:** Java 21, Spring Boot (Web, JPA, Mail)
- **Database:** PostgreSQL
- **Frontend:** HTML5, Vanilla CSS3 (Custom Animations), JavaScript, Thymeleaf
- **Build Tool:** Maven
