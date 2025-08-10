package com.example.maven.api.controller;

import com.example.maven.api.dto.request.comment.CommentCreateDto;
import com.example.maven.api.dto.request.comment.CommentUpdateDto;
import com.example.maven.api.dto.response.CommentResponseDto;
import com.example.maven.service.CommentService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@AllArgsConstructor
public class CommentController {
	private final CommentService commentService;

	@PostMapping
	@PreAuthorize("hasRole('MEMBER')")
	public ResponseEntity<CommentResponseDto> createComment(@RequestBody CommentCreateDto dto){
		return ResponseEntity.ok(commentService.createComment(dto));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('MEMBER')")
	public ResponseEntity<CommentResponseDto> getCommentById(@PathVariable long id){
		return ResponseEntity.ok(commentService.findCommentById(id));
	}

	@GetMapping
	@PreAuthorize("hasRole('MEMBER')")
	public ResponseEntity<List<CommentResponseDto>> getAllCommentsByTaskId(@RequestParam long taskId){
		return ResponseEntity.ok(commentService.findAllCommentsByTaskId(taskId));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('MEMBER')")
	public ResponseEntity<CommentResponseDto> updateMyCommentById(@PathVariable long id, @RequestBody CommentUpdateDto dto){
		return ResponseEntity.ok(commentService.updateMyCommentById(id, dto));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('MEMBER')")
	public ResponseEntity<Void> deleteMyCommentById(@PathVariable long id){
		commentService.deleteMyCommentById(id);
		return ResponseEntity.noContent().build();
	}

}
