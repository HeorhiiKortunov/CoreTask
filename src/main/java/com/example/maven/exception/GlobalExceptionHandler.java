package com.example.maven.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException; // ВАЖНО: из Spring Security
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
		return ResponseEntity
				.status(HttpStatus.NOT_FOUND)
				.body(Map.of(
						"timestamp", LocalDateTime.now().toString(),
						"status", HttpStatus.NOT_FOUND.value(),
						"error", HttpStatus.NOT_FOUND.getReasonPhrase(),
						"message", ex.getMessage()
				));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
		return ResponseEntity
				.status(HttpStatus.FORBIDDEN)
				.body(Map.of(
						"timestamp", LocalDateTime.now().toString(),
						"status", HttpStatus.FORBIDDEN.value(),
						"error", HttpStatus.FORBIDDEN.getReasonPhrase(),
						"message", ex.getMessage()
				));
	}

	@ExceptionHandler({ BadCredentialsException.class, AuthenticationException.class })
	public ResponseEntity<Map<String, Object>> handleAuth(AuthenticationException ex) {
		return ResponseEntity
				.status(HttpStatus.UNAUTHORIZED)
				.body(Map.of(
						"timestamp", LocalDateTime.now().toString(),
						"status", HttpStatus.UNAUTHORIZED.value(),
						"error", HttpStatus.UNAUTHORIZED.getReasonPhrase(),
						"message", ex.getMessage()
				));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach(error -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});
		return ResponseEntity.badRequest().body(errors);
	}
}
