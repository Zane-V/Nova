package com.novalearn.novalearn.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCourseForm {

    @NotBlank(message = "Course title is required.")
    private String title;

    @NotBlank(message = "Category is required.")
    private String category;

    @NotBlank(message = "Level is required.")
    private String level;

    @NotBlank(message = "Description is required.")
    private String description;

    @NotBlank(message = "Content type is required.")
    private String contentType;

    @Size(max = 5000, message = "Text content must not exceed 5000 characters.")
    private String textContent;
}