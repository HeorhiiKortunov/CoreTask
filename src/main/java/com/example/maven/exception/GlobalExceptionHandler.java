package com.example.maven.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
		return ResponseEntity
				.status(HttpStatus.FORBIDDEN)
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
}