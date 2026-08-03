package com.novalearn.novalearn.dto;

import com.novalearn.novalearn.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterForm {

    @NotBlank(message = "Full Name is required.")
    private String fullName;

    @NotBlank(message = "Nickname is required.")
    @Size(min = 2, max = 30, message = "Nickname must be between 2 and 30 characters.")
    private String nickname;

    @NotBlank(message = "Email Address is required.")
    @Email(message = "Please provide a valid email address.")
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, message = "Password must be at least 8 characters long.")
    private String password;

    private Role role = Role.STUDENT;
}