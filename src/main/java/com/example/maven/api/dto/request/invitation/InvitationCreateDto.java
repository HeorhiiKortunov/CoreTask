package com.example.maven.api.dto.request.invitation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InvitationCreateDto(
		@NotBlank @Email String email
) {}
