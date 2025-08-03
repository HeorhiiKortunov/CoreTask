package com.example.maven.api.dto.request.user;

import jakarta.validation.constraints.*;

public record UserCreateDto(
	@Positive(message = "Company id must be positive")
	long companyId,

	@NotBlank(message = "Username is required")
	String username,

	@NotBlank(message = "Displayed name is required")
	String displayedName,

	@NotBlank(message = "Email is required")
	@Email(message = "Email should be valid")
	String email,

	@NotBlank(message = "Password is required")
	@Size(min = 6, message = "Password must be at least 6 characters long")
	String password
) {}
