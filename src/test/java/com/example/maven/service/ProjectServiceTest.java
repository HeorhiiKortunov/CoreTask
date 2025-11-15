package com.example.maven.service;

import com.example.maven.api.dto.request.project.ProjectCreateDto;
import com.example.maven.api.dto.request.project.ProjectUpdateDto;
import com.example.maven.api.dto.response.ProjectResponseDto;
import com.example.maven.api.mapper.ProjectMapper;
import com.example.maven.exception.ResourceNotFoundException;
import com.example.maven.persistence.entity.Company;
import com.example.maven.persistence.entity.Project;
import com.example.maven.persistence.repository.CompanyRepository;
import com.example.maven.persistence.repository.ProjectRepository;
import com.example.maven.utils.SecurityUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectMapper projectMapper;
    @Mock private CompanyRepository companyRepository;
    @Mock private SecurityUtils securityUtils;

    @InjectMocks
    private ProjectService projectService;

    private static final long TENANT_ID = 123L;

    private Company company;

    @BeforeEach
    void setUp() {
        // Mock instance method instead of static
        when(securityUtils.getCurrentTenantId()).thenReturn(TENANT_ID);

        company = new Company();
        company.setId(TENANT_ID);
    }

    // No need for tearDown() anymore!

    // createProject
    @Test
    void givenValidDtoAndTenant_whenCreateProject_thenSavesWithCompany_andReturnsDto() {
        ProjectCreateDto dto = mock(ProjectCreateDto.class);
        Project mapped = new Project();
        Project saved = new Project();  saved.setId(10L);
        ProjectResponseDto response = mock(ProjectResponseDto.class);

        when(projectMapper.fromCreateDto(dto)).thenReturn(mapped);
        when(companyRepository.findById(TENANT_ID)).thenReturn(Optional.of(company));
        when(projectRepository.save(mapped)).thenReturn(saved);
        when(projectMapper.toResponseDto(saved)).thenReturn(response);

        ProjectResponseDto result = projectService.createProject(dto);

        assertThat(mapped.getCompany()).isEqualTo(company);
        assertThat(result).isEqualTo(response);
        verify(projectRepository).save(mapped);
    }

    @Test
    void givenNoCompanyForTenant_whenCreateProject_thenThrowsAccessDenied() {
        ProjectCreateDto dto = mock(ProjectCreateDto.class);
        when(projectMapper.fromCreateDto(dto)).thenReturn(new Project());
        when(companyRepository.findById(TENANT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.createProject(dto))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("No current company found");
        verify(projectRepository, never()).save(any());
    }

    // findById
    @Test
    void givenExistingProject_whenFindById_thenReturnDto() {
        Project p = new Project(); p.setId(5L);
        ProjectResponseDto dto = mock(ProjectResponseDto.class);

        when(projectRepository.findByIdAndCompany_Id(5L, TENANT_ID)).thenReturn(Optional.of(p));
        when(projectMapper.toResponseDto(p)).thenReturn(dto);

        assertThat(projectService.findById(5L)).isEqualTo(dto);
    }

    @Test
    void givenMissingProject_whenFindById_thenThrowNotFound() {
        when(projectRepository.findByIdAndCompany_Id(404L, TENANT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.findById(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project not found");
    }

    // findAllProjects
    @Test
    void givenTenant_whenFindAllProjects_thenMapsAll() {
        Project p1 = new Project(); p1.setId(1L);
        Project p2 = new Project(); p2.setId(2L);
        ProjectResponseDto d1 = mock(ProjectResponseDto.class);
        ProjectResponseDto d2 = mock(ProjectResponseDto.class);

        when(projectRepository.findAllByCompany_Id(TENANT_ID)).thenReturn(List.of(p1, p2));
        when(projectMapper.toResponseDto(p1)).thenReturn(d1);
        when(projectMapper.toResponseDto(p2)).thenReturn(d2);

        List<ProjectResponseDto> result = projectService.findAllProjects();

        assertThat(result).containsExactly(d1, d2);
        verify(projectRepository).findAllByCompany_Id(TENANT_ID);
    }

    // updateProject
    @Test
    void givenExistingProjectAndDto_whenUpdateProject_thenMapperApplies_andSaves_andReturnsDto() {
        long id = 7L;
        ProjectUpdateDto dto = mock(ProjectUpdateDto.class);
        Project existing = new Project(); existing.setId(id);
        Project saved = new Project(); saved.setId(id);
        ProjectResponseDto response = mock(ProjectResponseDto.class);

        when(projectRepository.findByIdAndCompany_Id(id, TENANT_ID)).thenReturn(Optional.of(existing));
        doAnswer(inv -> null).when(projectMapper).fromUpdateDto(existing, dto);
        when(projectRepository.save(existing)).thenReturn(saved);
        when(projectMapper.toResponseDto(saved)).thenReturn(response);

        ProjectResponseDto result = projectService.updateProject(id, dto);

        assertThat(result).isEqualTo(response);
        verify(projectMapper).fromUpdateDto(existing, dto);
        verify(projectRepository).save(existing);
    }

    @Test
    void givenMissingProject_whenUpdateProject_thenThrowNotFound() {
        when(projectRepository.findByIdAndCompany_Id(8L, TENANT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.updateProject(8L, mock(ProjectUpdateDto.class)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project not found");
        verify(projectRepository, never()).save(any());
    }

    // deleteProject
    @Test
    void givenExistingProject_whenDeleteProject_thenRepositoryDeleteCalled() {
        Project existing = new Project(); existing.setId(11L);
        when(projectRepository.findByIdAndCompany_Id(11L, TENANT_ID)).thenReturn(Optional.of(existing));

        projectService.deleteProject(11L);

        verify(projectRepository).delete(existing);
    }

    @Test
    void givenMissingProject_whenDeleteProject_thenThrowNotFound() {
        when(projectRepository.findByIdAndCompany_Id(12L, TENANT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.deleteProject(12L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project not found");
        verify(projectRepository, never()).delete(any());
    }
}