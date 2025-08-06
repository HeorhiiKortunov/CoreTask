package com.example.maven.api.dto.request.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CommentUpdateDto {
	@NotBlank
	private String contents;
}
