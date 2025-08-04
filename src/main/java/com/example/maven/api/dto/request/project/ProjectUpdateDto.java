package com.example.maven.api.dto.request.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProjectUpdateDto {
	@NotBlank(message = "Name is required")
	@Size(max = 20, message = "Password must be at least 6 characters long")
	private String name;

	@NotBlank
	private String description;
}
