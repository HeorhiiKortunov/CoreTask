package com.example.maven.api.controller;

import com.example.maven.api.dto.request.comment.CommentCreateDto;
import com.example.maven.api.dto.request.comment.CommentUpdateDto;
import com.example.maven.api.dto.response.CommentResponseDto;
import com.example.maven.service.CommentService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@AllArgsConstructor
public class CommentController {
	private final CommentService commentService;

	@PostMapping
	public ResponseEntity<CommentResponseDto> createComment(@RequestBody CommentCreateDto dto){
		return ResponseEntity.ok(commentService.createComment(dto));
	}

	@GetMapping("/{id}")
	public ResponseEntity<CommentResponseDto> getCommentById(@PathVariable long id){
		return ResponseEntity.ok(commentService.findCommentById(id));
	}

	@GetMapping
	public ResponseEntity<List<CommentResponseDto>> getAllCommentsByTaskId(@RequestParam long taskId){
		return ResponseEntity.ok(commentService.findAllCommentsByTaskId(taskId));
	}

	@PutMapping("/{id}")
	public ResponseEntity<CommentResponseDto> updateMyCommentById(@PathVariable long id, @RequestBody CommentUpdateDto dto){
		return ResponseEntity.ok(commentService.updateMyCommentById(id, dto));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteMyCommentById(@PathVariable long id){
		commentService.deleteMyCommentById(id);
		return ResponseEntity.noContent().build();
	}

}
