package com.novalearn.novalearn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.AuthorityUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/register", "/login", "/forgot-password", "/verify-code", "/reset-password", "/courses", "/css/**", "/js/**", "/images/**", "/assets/**", "/favicon.svg", "/favicon.ico").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/lecturer-dashboard", "/create-course", "/end-session/**", "/create-session").hasRole("LECTURER")
                .requestMatchers("/student-dashboard", "/enroll/**").hasRole("STUDENT")
                .requestMatchers("/course/*/delete", "/post/*/delete", "/post/*/edit").hasRole("LECTURER")
                .requestMatchers("/delete-account").authenticated()
                .anyRequest().authenticated()
            )
            .headers(headers -> headers
                .contentTypeOptions(contentType -> {})
                .xssProtection(xss -> {})
                .frameOptions(frame -> frame.sameOrigin())
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(customSuccessHandler())
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return (request, response, authentication) -> {
            Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
            String selectedRole = request.getParameter("role");

            boolean roleMatches = ("LECTURER".equals(selectedRole) && roles.contains("ROLE_LECTURER"))
                    || ("STUDENT".equals(selectedRole) && roles.contains("ROLE_STUDENT"));

            if (!roleMatches) {
                SecurityContextHolder.clearContext();
                if (request.getSession(false) != null) {
                    request.getSession(false).invalidate();
                }

                String email = URLEncoder.encode(request.getParameter("email"), StandardCharsets.UTF_8);
                String role = URLEncoder.encode(selectedRole == null ? "" : selectedRole, StandardCharsets.UTF_8);
                response.sendRedirect("/login?error=role&email=" + email + "&selectedRole=" + role);
                return;
            }

            if (roles.contains("ROLE_LECTURER")) {
                response.sendRedirect("/lecturer-dashboard");
            } else {
                response.sendRedirect("/student-dashboard");
            }
        };
    }
}
