package com.example.maven.api.dto.request.task;

import com.example.maven.enums.TaskStatus;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TaskUpdateDto {
	@Size(max = 30, message = "Name should be less than 30 characters long")
	private String name;

	private String description;

	private Long assigneeId;

	private LocalDateTime dueTo;

	private TaskStatus status;
}
