package com.novalearn.novalearn.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSessionForm {

    @NotBlank(message = "Session title is required.")
    private String title;

    @NotBlank(message = "Subject is required.")
    private String subject;
}