package com.example.maven.service;

import com.example.maven.api.dto.request.project.ProjectCreateDto;
import com.example.maven.api.dto.request.project.ProjectUpdateDto;
import com.example.maven.api.dto.response.ProjectResponseDto;
import com.example.maven.api.mapper.ProjectMapper;
import com.example.maven.exception.ResourceNotFoundException;
import com.example.maven.persistence.entity.Project;
import com.example.maven.persistence.repository.CompanyRepository;
import com.example.maven.persistence.repository.ProjectRepository;
import com.example.maven.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class ProjectService {
	private final ProjectRepository projectRepository;
	private final ProjectMapper projectMapper;
	private final CompanyRepository companyRepository;
	private final SecurityUtils securityUtils;

	// Evict projects list cache when creating
	@CacheEvict(value = "companyProjects", key = "@securityUtils.getCurrentTenantId()")
	public ProjectResponseDto createProject(ProjectCreateDto dto){
		var project = projectMapper.fromCreateDto(dto);
		project.setCompany(companyRepository.findById(securityUtils.getCurrentTenantId())
				.orElseThrow(() -> new AccessDeniedException("No current company found")));

		var savedProject = projectRepository.save(project);
		return projectMapper.toResponseDto(savedProject);
	}

	// Cache individual project by ID
	@Cacheable(value = "projects", key = "#id + '_' + @securityUtils.getCurrentTenantId()")
	public ProjectResponseDto findById(long id){
		return projectMapper.toResponseDto(getProjectById(id));
	}

	// Cache projects list per tenant
	@Cacheable(value = "companyProjects", key = "@securityUtils.getCurrentTenantId()")
	public List<ProjectResponseDto> findAllProjects(){
		return projectRepository.findAllByCompany_Id(securityUtils.getCurrentTenantId()).stream()
				.map(projectMapper::toResponseDto)
				.toList();
	}

	// Evict both individual project cache and projects list cache
	@Caching(evict = {
			@CacheEvict(value = "projects", key = "#id + '_' + @securityUtils.getCurrentTenantId()"),
			@CacheEvict(value = "companyProjects", key = "@securityUtils.getCurrentTenantId()")
	})
	public ProjectResponseDto updateProject(long id, ProjectUpdateDto dto){
		var project = getProjectById(id);
		projectMapper.fromUpdateDto(project, dto);
		var savedProject = projectRepository.save(project);
		return projectMapper.toResponseDto(savedProject);
	}

	// Evict both individual project cache and projects list cache
	@Caching(evict = {
			@CacheEvict(value = "projects", key = "#id + '_' + @securityUtils.getCurrentTenantId()"),
			@CacheEvict(value = "companyProjects", key = "@securityUtils.getCurrentTenantId()")
	})
	public void deleteProject(long id){
		projectRepository.delete(getProjectById(id));
	}

	private Project getProjectById(long id){
		return projectRepository.findByIdAndCompany_Id(id, securityUtils.getCurrentTenantId())
				.orElseThrow(() -> new ResourceNotFoundException("Project not found"));
	}
}