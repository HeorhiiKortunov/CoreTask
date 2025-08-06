package com.example.maven.api.controller;

import com.example.maven.api.dto.request.task.TaskCreateDto;
import com.example.maven.api.dto.request.task.TaskUpdateDto;
import com.example.maven.api.dto.response.TaskResponseDto;
import com.example.maven.service.TaskService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@AllArgsConstructor
public class TaskController {
	private final TaskService taskService;

	@PostMapping
	public ResponseEntity<TaskResponseDto> createTask(@RequestBody TaskCreateDto dto){
		return ResponseEntity.ok(taskService.createTask(dto));
	}

	@GetMapping
	public ResponseEntity<List<TaskResponseDto>> getTasksByProject(
			@RequestParam(required = false) Long projectId
	){
		return ResponseEntity.ok(taskService.findAllTasksByProjectId(projectId));
	}

	@GetMapping("/{id}")
	public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable long id){
		return ResponseEntity.ok(taskService.findTaskById(id));
	}

	@PatchMapping("/{id}")
	public ResponseEntity<TaskResponseDto> updateTaskById(@PathVariable long id, @RequestBody TaskUpdateDto dto){
		return ResponseEntity.ok(taskService.updateTaskById(id, dto));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteTaskById(@PathVariable long id){
		taskService.deleteTaskById(id);
		return ResponseEntity.noContent().build();
	}
}
