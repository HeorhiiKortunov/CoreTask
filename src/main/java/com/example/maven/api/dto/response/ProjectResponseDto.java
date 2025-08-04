package com.example.maven.api.dto.response;

import java.time.LocalDateTime;

public record ProjectResponseDto(
		long id,
		String name,
		String description,
		LocalDateTime createdAt
) {}
