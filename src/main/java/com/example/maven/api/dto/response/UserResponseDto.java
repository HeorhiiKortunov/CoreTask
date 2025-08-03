package com.example.maven.api.dto.response;

import java.util.Set;

public record UserResponseDto(
	long id,
	String username,
	String displayedName,
	String email,
	Set<String> roles
) {}
