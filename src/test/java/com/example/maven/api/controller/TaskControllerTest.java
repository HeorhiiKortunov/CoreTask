package com.example.maven.api.controller;

import com.example.maven.api.dto.request.task.TaskCreateDto;
import com.example.maven.api.dto.request.task.TaskUpdateDto;
import com.example.maven.api.dto.response.TaskResponseDto;
import com.example.maven.enums.TaskStatus;
import com.example.maven.security.WebSecurityConfig;
import com.example.maven.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = true)
@Import(WebSecurityConfig.class)
class TaskControllerTest {

	@Autowired MockMvc mockMvc;

	@MockitoBean TaskService taskService;

	// ---------- POST /api/tasks (ADMIN) ----------
	@WithMockTenantUser(roles = { com.example.maven.enums.Role.ROLE_ADMIN })
	@Test
	void givenAdmin_whenCreateTask_then200_andServiceCalled() throws Exception {
		String json = """
            {
              "name": "Task A",
              "description": "Do A",
              "projectId": 5,
              "assigneeId": 10,
              "dueTo": "2030-01-01T00:00:00"
            }
            """;

		var now = LocalDateTime.now();
		var resp = new TaskResponseDto(
				1L, "Task A", "Do A", 10L, TaskStatus.TODO, now, LocalDateTime.parse("2030-01-01T00:00:00")
		);
		when(taskService.createTask(any(TaskCreateDto.class))).thenReturn(resp);

		mockMvc.perform(post("/api/tasks")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.name").value("Task A"))
				.andExpect(jsonPath("$.assigneeId").value(10));
		verify(taskService).createTask(any(TaskCreateDto.class));
	}

	@WithMockTenantUser(roles = { com.example.maven.enums.Role.ROLE_MEMBER })
	@Test
	void givenMember_whenCreateTask_then403() throws Exception {
		String json = """
            { "name":"X", "description":"Y", "projectId":1 }
            """;

		mockMvc.perform(post("/api/tasks")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isForbidden());
	}

	@Test
	void givenNoAuth_whenCreateTask_then401() throws Exception {
		String json = """
            { "name":"X", "description":"Y", "projectId":1 }
            """;

		mockMvc.perform(post("/api/tasks")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isUnauthorized());
	}

	// ---------- GET /api/tasks?projectId=... (MEMBER) ----------
	@WithMockTenantUser(roles = { com.example.maven.enums.Role.ROLE_MEMBER })
	@Test
	void givenMember_whenGetTasks_withoutProject_then200AndList() throws Exception {
		var now = LocalDateTime.now();
		var t1 = new TaskResponseDto(1L, "T1", "D1", 0L, TaskStatus.TODO, now, now.plusDays(1));
		var t2 = new TaskResponseDto(2L, "T2", "D2", 5L, TaskStatus.IN_PROGRESS, now, now.plusDays(2));

		when(taskService.findAllTasksByProjectId(isNull())).thenReturn(List.of(t1, t2));

		mockMvc.perform(get("/api/tasks"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", Matchers.hasSize(2)))
				.andExpect(jsonPath("$[0].id").value(1L))
				.andExpect(jsonPath("$[1].id").value(2L));
	}

	@WithMockTenantUser(roles = { com.example.maven.enums.Role.ROLE_MEMBER })
	@Test
	void givenMember_whenGetTasks_withProject_then200AndFilteredList() throws Exception {
		var now = LocalDateTime.now();
		var t = new TaskResponseDto(7L, "ProjTask", "D", 3L, TaskStatus.DONE, now, now.plusDays(3));

		when(taskService.findAllTasksByProjectId(eq(55L))).thenReturn(List.of(t));

		mockMvc.perform(get("/api/tasks").param("projectId", "55"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", Matchers.hasSize(1)))
				.andExpect(jsonPath("$[0].id").value(7L));
	}

	@WithMockTenantUser(roles = { com.example.maven.enums.Role.ROLE_ADMIN }) // нет ROLE_MEMBER → 403
	@Test
	void givenAdminOnly_whenGetTasks_then403() throws Exception {
		mockMvc.perform(get("/api/tasks"))
				.andExpect(status().isForbidden());
	}

	@Test
	void givenNoAuth_whenGetTasks_then401() throws Exception {
		mockMvc.perform(get("/api/tasks"))
				.andExpect(status().isUnauthorized());
	}

	// ---------- GET /api/tasks/{id} (MEMBER) ----------
	@WithMockTenantUser(roles = { com.example.maven.enums.Role.ROLE_MEMBER })
	@Test
	void givenMember_whenGetTaskById_then200AndBody() throws Exception {
		var now = LocalDateTime.now();
		var resp = new TaskResponseDto(99L, "Read", "Book", 0L, TaskStatus.TODO, now, now.plusDays(10));
		when(taskService.findTaskById(99L)).thenReturn(resp);

		mockMvc.perform(get("/api/tasks/{id}", 99L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(99L))
				.andExpect(jsonPath("$.name").value("Read"))
				.andExpect(jsonPath("$.description").value("Book"));
	}

	@Test
	void givenNoAuth_whenGetTaskById_then401() throws Exception {
		mockMvc.perform(get("/api/tasks/{id}", 99L))
				.andExpect(status().isUnauthorized());
	}

	// ---------- PATCH /api/tasks/{id} (ADMIN) ----------
	@WithMockTenantUser(roles = { com.example.maven.enums.Role.ROLE_ADMIN })
	@Test
	void givenAdmin_whenUpdateTask_then200AndBody() throws Exception {
		String json = """
            {
              "name": "New name",
              "description": "New desc",
              "status": "IN_PROGRESS",
              "assigneeId": 22
            }
            """;

		var now = LocalDateTime.now();
		var resp = new TaskResponseDto(5L, "New name", "New desc", 22L, TaskStatus.IN_PROGRESS, now, now.plusDays(5));
		when(taskService.updateTaskById(eq(5L), any(TaskUpdateDto.class))).thenReturn(resp);

		mockMvc.perform(patch("/api/tasks/{id}", 5L)
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(5L))
				.andExpect(jsonPath("$.name").value("New name"))
				.andExpect(jsonPath("$.assigneeId").value(22));
	}

	@WithMockTenantUser(roles = { com.example.maven.enums.Role.ROLE_MEMBER })
	@Test
	void givenMember_whenUpdateTask_then403() throws Exception {
		String json = """
            { "name": "X" }
            """;

		mockMvc.perform(patch("/api/tasks/{id}", 5L)
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isForbidden());
	}

	@Test
	void givenNoAuth_whenUpdateTask_then401() throws Exception {
		String json = """
            { "name": "X" }
            """;

		mockMvc.perform(patch("/api/tasks/{id}", 5L)
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isUnauthorized());
	}

	// ---------- DELETE /api/tasks/{id} (ADMIN) ----------
	@WithMockTenantUser(roles = { com.example.maven.enums.Role.ROLE_ADMIN })
	@Test
	void givenAdmin_whenDeleteTask_then204_andServiceCalled() throws Exception {
		mockMvc.perform(delete("/api/tasks/{id}", 44L))
				.andExpect(status().isNoContent());

		verify(taskService).deleteTaskById(44L);
	}

	@WithMockTenantUser(roles = { com.example.maven.enums.Role.ROLE_MEMBER })
	@Test
	void givenMember_whenDeleteTask_then403() throws Exception {
		mockMvc.perform(delete("/api/tasks/{id}", 44L))
				.andExpect(status().isForbidden());
	}

	@Test
	void givenNoAuth_whenDeleteTask_then401() throws Exception {
		mockMvc.perform(delete("/api/tasks/{id}", 44L))
				.andExpect(status().isUnauthorized());
	}
}
