package com.example.maven.service;

import com.example.maven.api.dto.request.task.TaskCreateDto;
import com.example.maven.api.dto.request.task.TaskUpdateDto;
import com.example.maven.api.dto.response.TaskResponseDto;
import com.example.maven.api.mapper.TaskMapper;
import com.example.maven.enums.TaskStatus;
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
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

	@Mock private TaskRepository taskRepository;
	@Mock private TaskMapper taskMapper;
	@Mock private ProjectRepository projectRepository;
	@Mock private CompanyRepository companyRepository;
	@Mock private UserRepository userRepository;
	@Mock private SecurityUtils securityUtils;

	@InjectMocks
	private TaskService taskService;

	private static final long TENANT_ID = 77L;

	private Company company;
	private Project project;

	private static final LocalDateTime BASE_TIME = LocalDateTime.of(2025, 1, 1, 10, 0);

	@BeforeEach
	void setUp() {
		// Mock instance method instead of static
		when(securityUtils.getCurrentTenantId()).thenReturn(TENANT_ID);

		company = new Company(); company.setId(TENANT_ID);
		project = new Project(); project.setId(10L);
	}

	// No need for tearDown() anymore!

	// createTask
	@Test
	void givenValidDtoWithAssignee_whenCreateTask_thenSavesAndReturnsDto() {
		// given
		TaskCreateDto dto = mock(TaskCreateDto.class);
		when(dto.getProjectId()).thenReturn(10L);
		when(dto.getAssigneeId()).thenReturn(3L);

		User assignee = new User(); assignee.setId(3L);
		Task mapped = new Task();
		Task saved = new Task(); saved.setId(100L);
		TaskResponseDto response = new TaskResponseDto(
				100L, "Task", "Desc", 3L, TaskStatus.TODO, BASE_TIME, BASE_TIME.plusDays(7));

		when(projectRepository.findByIdAndCompany_Id(10L, TENANT_ID)).thenReturn(Optional.of(project));
		when(companyRepository.findById(TENANT_ID)).thenReturn(Optional.of(company));
		when(userRepository.findByIdAndCompany_Id(3L, TENANT_ID)).thenReturn(Optional.of(assignee));
		when(taskMapper.fromCreateDto(dto, project, company, assignee)).thenReturn(mapped);
		when(taskRepository.save(mapped)).thenReturn(saved);
		when(taskMapper.toResponseDto(saved)).thenReturn(response);

		// when
		TaskResponseDto result = taskService.createTask(dto);

		// then
		assertThat(result).isEqualTo(response);
		verify(taskMapper).fromCreateDto(dto, project, company, assignee);
		verify(taskRepository).save(mapped);
	}

	@Test
	void givenValidDtoWithoutAssignee_whenCreateTask_thenAssigneeIdZeroAndSaved() {
		TaskCreateDto dto = mock(TaskCreateDto.class);
		when(dto.getProjectId()).thenReturn(10L);
		when(dto.getAssigneeId()).thenReturn(0L);

		Task mapped = new Task();
		Task saved = new Task(); saved.setId(101L);
		TaskResponseDto response = new TaskResponseDto(
				101L, "Task", "Desc", 0L, TaskStatus.TODO, BASE_TIME, BASE_TIME.plusDays(3));

		when(projectRepository.findByIdAndCompany_Id(10L, TENANT_ID)).thenReturn(Optional.of(project));
		when(companyRepository.findById(TENANT_ID)).thenReturn(Optional.of(company));
		when(taskMapper.fromCreateDto(eq(dto), eq(project), eq(company), isNull())).thenReturn(mapped);
		when(taskRepository.save(mapped)).thenReturn(saved);
		when(taskMapper.toResponseDto(saved)).thenReturn(response);

		TaskResponseDto result = taskService.createTask(dto);

		assertThat(result).isEqualTo(response);
		verifyNoInteractions(userRepository);
	}

	@Test
	void givenMissingProject_whenCreateTask_thenThrowNotFound() {
		TaskCreateDto dto = mock(TaskCreateDto.class);
		when(dto.getProjectId()).thenReturn(999L);

		when(projectRepository.findByIdAndCompany_Id(999L, TENANT_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> taskService.createTask(dto))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Project not found");
	}

	@Test
	void givenMissingCompany_whenCreateTask_thenThrowNotFound() {
		TaskCreateDto dto = mock(TaskCreateDto.class);
		when(dto.getProjectId()).thenReturn(10L);

		when(projectRepository.findByIdAndCompany_Id(10L, TENANT_ID)).thenReturn(Optional.of(project));
		when(companyRepository.findById(TENANT_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> taskService.createTask(dto))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("No current company found");
	}

	@Test
	void givenMissingAssignee_whenCreateTask_thenThrowNotFound() {
		TaskCreateDto dto = mock(TaskCreateDto.class);
		when(dto.getProjectId()).thenReturn(10L);
		when(dto.getAssigneeId()).thenReturn(5L);

		when(projectRepository.findByIdAndCompany_Id(10L, TENANT_ID)).thenReturn(Optional.of(project));
		when(companyRepository.findById(TENANT_ID)).thenReturn(Optional.of(company));
		when(userRepository.findByIdAndCompany_Id(5L, TENANT_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> taskService.createTask(dto))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Assignee not found");
	}

	// findAllTasksByProjectId
	@Test
	void givenNullProjectId_whenFindAllTasksByProjectId_thenReturnAllByCompany() {
		Task t1 = new Task(); t1.setId(1L);
		Task t2 = new Task(); t2.setId(2L);
		TaskResponseDto d1 = new TaskResponseDto(1L, "A", "dA", 0L, TaskStatus.TODO, BASE_TIME, BASE_TIME.plusDays(1));
		TaskResponseDto d2 = new TaskResponseDto(2L, "B", "dB", 0L, TaskStatus.IN_PROGRESS, BASE_TIME, BASE_TIME.plusDays(2));

		when(taskRepository.findAllByCompany_Id(TENANT_ID)).thenReturn(List.of(t1, t2));
		when(taskMapper.toResponseDto(t1)).thenReturn(d1);
		when(taskMapper.toResponseDto(t2)).thenReturn(d2);

		List<TaskResponseDto> result = taskService.findAllTasksByProjectId(null);

		assertThat(result).containsExactly(d1, d2);
		verify(taskRepository).findAllByCompany_Id(TENANT_ID);
	}

	@Test
	void givenProjectId_whenFindAllTasksByProjectId_thenReturnOnlyProjectTasks() {
		Task t1 = new Task(); t1.setId(10L);
		TaskResponseDto d1 = new TaskResponseDto(10L, "P", "dp", 0L, TaskStatus.DONE, BASE_TIME, BASE_TIME.plusDays(5));

		when(taskRepository.findAllByCompany_IdAndProject_Id(TENANT_ID, 55L)).thenReturn(List.of(t1));
		when(taskMapper.toResponseDto(t1)).thenReturn(d1);

		List<TaskResponseDto> result = taskService.findAllTasksByProjectId(55L);

		assertThat(result).containsExactly(d1);
		verify(taskRepository).findAllByCompany_IdAndProject_Id(TENANT_ID, 55L);
	}

	// findTaskById
	@Test
	void givenExistingTask_whenFindTaskById_thenReturnDto() {
		Task t = new Task(); t.setId(7L);
		TaskResponseDto dto = new TaskResponseDto(7L, "X", "dx", 0L, TaskStatus.TODO, BASE_TIME, BASE_TIME.plusDays(2));

		when(taskRepository.findByIdAndCompany_Id(7L, TENANT_ID)).thenReturn(Optional.of(t));
		when(taskMapper.toResponseDto(t)).thenReturn(dto);

		assertThat(taskService.findTaskById(7L)).isEqualTo(dto);
	}

	@Test
	void givenMissingTask_whenFindTaskById_thenThrowNotFound() {
		when(taskRepository.findByIdAndCompany_Id(404L, TENANT_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> taskService.findTaskById(404L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Task not found");
	}

	// updateTaskById
	@Test
	void givenExistingTaskAndDto_whenUpdateTaskById_thenMapperAppliesAndSaves() {
		long id = 12L;
		TaskUpdateDto dto = mock(TaskUpdateDto.class);
		Task existing = new Task(); existing.setId(id);
		Task saved = new Task(); saved.setId(id);
		TaskResponseDto resp = new TaskResponseDto(id, "Upd", "du", 0L, TaskStatus.IN_PROGRESS, BASE_TIME, BASE_TIME.plusDays(4));

		when(taskRepository.findByIdAndCompany_Id(id, TENANT_ID)).thenReturn(Optional.of(existing));
		doAnswer(inv -> null).when(taskMapper).updateFromDto(existing, dto);
		when(taskRepository.save(existing)).thenReturn(saved);
		when(taskMapper.toResponseDto(saved)).thenReturn(resp);

		TaskResponseDto result = taskService.updateTaskById(id, dto);

		assertThat(result).isEqualTo(resp);
		verify(taskMapper).updateFromDto(existing, dto);
		verify(taskRepository).save(existing);
	}

	@Test
	void givenMissingTask_whenUpdateTaskById_thenThrowNotFound() {
		when(taskRepository.findByIdAndCompany_Id(13L, TENANT_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> taskService.updateTaskById(13L, mock(TaskUpdateDto.class)))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Task not found");
	}

	// deleteTaskById
	@Test
	void givenExistingTask_whenDeleteTaskById_thenRepositoryDeleteCalled() {
		Task t = new Task(); t.setId(20L);
		when(taskRepository.findByIdAndCompany_Id(20L, TENANT_ID)).thenReturn(Optional.of(t));

		taskService.deleteTaskById(20L);

		verify(taskRepository).delete(t);
	}

	@Test
	void givenMissingTask_whenDeleteTaskById_thenThrowNotFound() {
		when(taskRepository.findByIdAndCompany_Id(21L, TENANT_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> taskService.deleteTaskById(21L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Task not found");
	}
}