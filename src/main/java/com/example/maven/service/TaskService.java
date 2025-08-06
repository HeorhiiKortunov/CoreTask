package com.example.maven.service;

import com.example.maven.api.dto.request.task.TaskCreateDto;
import com.example.maven.api.dto.request.task.TaskUpdateDto;
import com.example.maven.api.dto.response.TaskResponseDto;
import com.example.maven.api.mapper.TaskMapper;
import com.example.maven.exception.ResourceNotFoundException;
import com.example.maven.persistence.entity.Company;
import com.example.maven.persistence.entity.Project;
import com.example.maven.persistence.entity.Task;
import com.example.maven.persistence.entity.User;
import com.example.maven.persistence.repository.CompanyRepository;
import com.example.maven.persistence.repository.ProjectRepository;
import com.example.maven.persistence.repository.TaskRepository;
import com.example.maven.persistence.repository.UserRepository;
import com.example.maven.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class TaskService {
	private final TaskRepository taskRepository;
	private final TaskMapper taskMapper;
	private final ProjectRepository projectRepository;
	private final CompanyRepository companyRepository;
	private final UserRepository userRepository;

	public TaskResponseDto createTask(TaskCreateDto dto){
		long tenantId = SecurityUtils.getCurrentTenantId();

		Project project = projectRepository.findByIdAndCompany_Id(dto.getProjectId(), tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Project not found"));

		Company company = companyRepository.findById(tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("No current company found"));

		User assignee = null;
		if (dto.getAssigneeId() > 0) {
			assignee = userRepository.findByIdAndCompany_Id(dto.getAssigneeId(), tenantId)
					.orElseThrow(() -> new ResourceNotFoundException("Assignee not found"));
		}

		Task task = taskMapper.fromCreateDto(dto, project, company, assignee);

		Task savedTask = taskRepository.save(task);
		return taskMapper.toResponseDto(savedTask);
	}

	public List<TaskResponseDto> findAllTasksByProjectId(Long projectId){
		long tenantId = SecurityUtils.getCurrentTenantId();

		if(projectId == null){
			return taskRepository.findAllByCompany_Id(tenantId).stream()
					.map(taskMapper::toResponseDto)
					.toList();
		}

		return taskRepository.findAllByCompany_IdAndProject_Id(tenantId, projectId).stream()
				.map(taskMapper::toResponseDto)
				.toList();
	}

	public TaskResponseDto findTaskById(long id){
		long tenantId = SecurityUtils.getCurrentTenantId();
		return taskMapper.toResponseDto(taskRepository.findByIdAndCompany_Id(id, tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Task not found")));
	}

	public TaskResponseDto updateTaskById(long id, TaskUpdateDto dto){
		long tenantId = SecurityUtils.getCurrentTenantId();
		Task task = taskRepository.findByIdAndCompany_Id(id, tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Task not found"));

		taskMapper.updateFromDto(task, dto);
		Task savedTask = taskRepository.save(task);

		return taskMapper.toResponseDto(savedTask);
	}

	public void deleteTaskById(long id){
		long tenantId = SecurityUtils.getCurrentTenantId();
		Task task = taskRepository.findByIdAndCompany_Id(id, tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Task not found"));

		taskRepository.delete(task);
	}
}
