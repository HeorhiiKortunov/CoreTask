package com.example.maven.api.dto.request.company;

import com.example.maven.api.dto.request.user.UserCreateDto;
import jakarta.validation.constraints.NotBlank;

public record CompanyCreateDto(
		@NotBlank(message = "Company name is required")
		String name,

		UserCreateDto firstAdmin
) {}
