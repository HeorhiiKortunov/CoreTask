package com.example.maven.api.dto.request.user;

import jakarta.validation.constraints.Email;
import lombok.Getter;

@Getter
public class UserUpdateDto {
	private String displayedName;

	@Email(message = "Email should be valid")
	private String email;
}
