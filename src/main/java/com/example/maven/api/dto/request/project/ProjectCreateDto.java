package com.example.maven.api.dto.request.project;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectCreateDto(
		@NotBlank(message = "Name is required")
		@Size(max = 20, message = "Password must be at least 6 characters long")
		String name,

		@NotBlank
		String description
) {}
