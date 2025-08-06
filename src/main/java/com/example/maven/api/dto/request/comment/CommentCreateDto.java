package com.example.maven.api.dto.request.comment;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateDto(
	long taskId,

	@NotBlank
	String contents
) {}
