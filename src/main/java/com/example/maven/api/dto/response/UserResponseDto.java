package com.example.maven.api.dto.response;

import com.example.maven.enums.Role;

import java.util.Set;

public record UserResponseDto(
	long id,
	String username,
	String displayedName,
	String email,
	Set<Role> roles
) {}
