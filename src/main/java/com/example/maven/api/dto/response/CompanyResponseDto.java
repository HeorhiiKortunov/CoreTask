package com.example.maven.api.dto.response;

import java.time.LocalDateTime;

public record CompanyResponseDto(
		long id,
		String name,
		LocalDateTime createdAt
) {}
