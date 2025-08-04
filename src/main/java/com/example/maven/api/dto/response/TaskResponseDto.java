package com.example.maven.api.dto.response;

import com.example.maven.enums.TaskStatus;

import java.time.LocalDateTime;

public record TaskResponseDto(
		long id,
		String name,
		String description,
		long assigneeId,
		TaskStatus status,
		LocalDateTime createdAt,
		LocalDateTime dueTo
) {}
