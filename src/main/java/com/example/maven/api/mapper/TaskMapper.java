package com.example.maven.api.mapper;

import com.example.maven.api.dto.request.task.TaskCreateDto;
import com.example.maven.api.dto.request.task.TaskUpdateDto;
import com.example.maven.api.dto.response.TaskResponseDto;
import com.example.maven.enums.TaskStatus;
import com.example.maven.persistence.entity.Company;
import com.example.maven.persistence.entity.Project;
import com.example.maven.persistence.entity.Task;
import com.example.maven.persistence.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {
	public TaskResponseDto toResponseDto(Task task){
		return new TaskResponseDto(
				task.getId(),
				task.getName(),
				task.getDescription(),
				task.getAssignee().getId(),
				task.getStatus(),
				task.getCreatedAt(),
				task.getDueTo()
		);
	}

	public Task fromCreateDto(TaskCreateDto dto, Project project, Company company, User assignee) {
		Task task = new Task();
		task.setName(dto.getName());
		task.setDescription(dto.getDescription());
		task.setDueTo(dto.getDueTo());
		task.setStatus(TaskStatus.TODO);
		task.setProject(project);
		task.setCompany(company);
		task.setAssignee(assignee);
		return task;
	}

	public void updateFromDto(Task task, TaskUpdateDto dto){
		if(dto.getName() != null) task.setName(dto.getName());
		if(dto.getDescription() != null) task.setDescription(dto.getDescription());
		if(dto.getDueTo() != null) task.setDueTo(dto.getDueTo());
		if(dto.getStatus() != null) task.setStatus(dto.getStatus());
	}
}
