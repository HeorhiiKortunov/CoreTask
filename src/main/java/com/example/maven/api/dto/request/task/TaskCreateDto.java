package com.example.maven.api.dto.request.task;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class TaskCreateDto {
	@PositiveOrZero(message = "Project id must be a positive(or zero) number")
	private long projectId;

	@NotBlank(message = "Name is required")
	@Size(max = 30, message = "Name should be less than 30 characters long")
	private String name;

	@NotBlank(message = "Description is required")
	private String description;

	private long assigneeId;

	private LocalDateTime dueTo;
}
