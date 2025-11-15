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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
	private final SecurityUtils securityUtils;

	// Evict all task list caches when creating (allEntries since we don't know which projectId caches exist)
	@CacheEvict(value = "projectTasks", allEntries = true)
	public TaskResponseDto createTask(TaskCreateDto dto){
		long tenantId = securityUtils.getCurrentTenantId();

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

	// Cache tasks by project - separate cache for each projectId (or null for all tasks)
	@Cacheable(value = "projectTasks", key = "@securityUtils.getCurrentTenantId() + '_' + (#projectId != null ? #projectId : 'all')")
	public List<TaskResponseDto> findAllTasksByProjectId(Long projectId){
		long tenantId = securityUtils.getCurrentTenantId();

		if(projectId == null){
			return taskRepository.findAllByCompany_Id(tenantId).stream()
					.map(taskMapper::toResponseDto)
					.toList();
		}

		return taskRepository.findAllByCompany_IdAndProject_Id(tenantId, projectId).stream()
				.map(taskMapper::toResponseDto)
				.toList();
	}

	// Cache individual task by ID
	@Cacheable(value = "tasks", key = "#id + '_' + @securityUtils.getCurrentTenantId()")
	public TaskResponseDto findTaskById(long id){
		long tenantId = securityUtils.getCurrentTenantId();
		return taskMapper.toResponseDto(taskRepository.findByIdAndCompany_Id(id, tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Task not found")));
	}

	// Evict both individual task cache and all project task list caches
	@Caching(evict = {
			@CacheEvict(value = "tasks", key = "#id + '_' + @securityUtils.getCurrentTenantId()"),
			@CacheEvict(value = "projectTasks", allEntries = true)
	})
	public TaskResponseDto updateTaskById(long id, TaskUpdateDto dto){
		long tenantId = securityUtils.getCurrentTenantId();
		Task task = taskRepository.findByIdAndCompany_Id(id, tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Task not found"));

		taskMapper.updateFromDto(task, dto);
		Task savedTask = taskRepository.save(task);

		return taskMapper.toResponseDto(savedTask);
	}

	// Evict both individual task cache and all project task list caches
	@Caching(evict = {
			@CacheEvict(value = "tasks", key = "#id + '_' + @securityUtils.getCurrentTenantId()"),
			@CacheEvict(value = "projectTasks", allEntries = true)
	})
	public void deleteTaskById(long id){
		long tenantId = securityUtils.getCurrentTenantId();
		Task task = taskRepository.findByIdAndCompany_Id(id, tenantId)
				.orElseThrow(() -> new ResourceNotFoundException("Task not found"));

		taskRepository.delete(task);
	}
}