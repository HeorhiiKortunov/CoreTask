package com.example.maven.api.dto.request.user;

import com.example.maven.enums.Role;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record UserUpdateRolesDto(
		@NotBlank(message = "Roles are required")
		Set<Role> roles
) {}
