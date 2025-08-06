package com.example.maven.api.mapper;

import com.example.maven.api.dto.request.project.ProjectCreateDto;
import com.example.maven.api.dto.request.project.ProjectUpdateDto;
import com.example.maven.api.dto.response.ProjectResponseDto;
import com.example.maven.persistence.entity.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

	public Project fromCreateDto(ProjectCreateDto dto){
		Project project = new Project();
		project.setName(dto.name());
		project.setDescription(dto.description());

		return project;
	}

	public void fromUpdateDto(Project project, ProjectUpdateDto dto){
		if(dto.getDescription() != null) project.setDescription(dto.getDescription());
		if(dto.getName() != null) project.setDescription(dto.getDescription());
	}

	public ProjectResponseDto toResponseDto(Project project){
		return new ProjectResponseDto(
			project.getId(),
			project.getName(),
			project.getDescription(),
			project.getCreatedAt()
		);
	}
}
