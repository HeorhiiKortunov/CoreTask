package com.example.maven.api.dto.response;

public record CommentResponseDto(
		long id,
		long taskId,
		long authorId,
		String contents
) {}
