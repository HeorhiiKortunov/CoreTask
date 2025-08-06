package com.example.maven.api.controller;

import com.example.maven.api.dto.request.project.ProjectCreateDto;
import com.example.maven.api.dto.request.project.ProjectUpdateDto;
import com.example.maven.api.dto.response.ProjectResponseDto;
import com.example.maven.service.ProjectService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/projects")
public class ProjectController {
	private final ProjectService projectService;

	@PostMapping
	public ResponseEntity<ProjectResponseDto> createProject(@RequestBody ProjectCreateDto dto){
		return ResponseEntity.ok(projectService.createProject(dto));
	}

	@GetMapping
	public ResponseEntity<List<ProjectResponseDto>> getAllProjects(){
		return ResponseEntity.ok(projectService.findAllProjects());
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProjectResponseDto> getProject(@PathVariable long id){
		return ResponseEntity.ok(projectService.findById(id));
	}

	@PatchMapping("/{id}")
	public ResponseEntity<ProjectResponseDto> updateProject(@PathVariable long id, @RequestBody ProjectUpdateDto dto){
		return ResponseEntity.ok(projectService.updateProject(id, dto));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteProject(@PathVariable long id){
		projectService.deleteProject(id);
		return ResponseEntity.noContent().build();
	}

}
