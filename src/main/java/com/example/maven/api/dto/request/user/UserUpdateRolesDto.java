package com.example.maven.api.dto.request.user;

import com.example.maven.enums.Role;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record UserUpdateRolesDto(
		@NotEmpty(message = "roles must not be empty")
		Set<@NotNull(message = "role must not be null") Role> roles
) {}
