package com.example.maven.api.dto.request.project;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ProjectUpdateDto {
	@Size(max = 20, message = "Password must be at least 6 characters long")
	private String name;

	private String description;
}
