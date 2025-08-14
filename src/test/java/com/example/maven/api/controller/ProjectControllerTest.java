package com.example.maven.api.controller;

import com.example.maven.api.dto.request.project.ProjectCreateDto;
import com.example.maven.api.dto.request.project.ProjectUpdateDto;
import com.example.maven.api.dto.response.ProjectResponseDto;
import com.example.maven.enums.Role;
import com.example.maven.security.WebSecurityConfig;
import com.example.maven.service.ProjectService;
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
class ProjectControllerTest {

	@Autowired MockMvc mockMvc;
	@Autowired ObjectMapper objectMapper;

	@MockitoBean ProjectService projectService;

	// ---------- POST /api/projects (ADMIN) ----------
	@WithMockTenantUser(roles = { Role.ROLE_ADMIN })
	@Test
	void givenAdmin_whenCreateProject_then200() throws Exception {
		String json = """
            { "name": "New Project", "description": "Desc" }
            """;

		var resp = new ProjectResponseDto(1L, "New Project", "Desc", LocalDateTime.of(2025, 1, 1, 10, 0));
		when(projectService.createProject(any(ProjectCreateDto.class))).thenReturn(resp);

		mockMvc.perform(post("/api/projects")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.name").value("New Project"))
				.andExpect(jsonPath("$.description").value("Desc"))
				.andExpect(jsonPath("$.createdAt").value("2025-01-01T10:00:00"));
	}

	@WithMockTenantUser(roles = { Role.ROLE_MEMBER })
	@Test
	void givenMember_whenCreateProject_then403() throws Exception {
		String json = """
            { "name": "New Project" }
            """;

		mockMvc.perform(post("/api/projects")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isForbidden());
	}

	@Test
	void givenNoAuth_whenCreateProject_then401() throws Exception {
		String json = """
            { "name": "New Project" }
            """;

		mockMvc.perform(post("/api/projects")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isUnauthorized());
	}

	// ---------- GET /api/projects (MEMBER) ----------
	@WithMockTenantUser(roles = { Role.ROLE_MEMBER })
	@Test
	void givenMember_whenGetAllProjects_then200AndList() throws Exception {
		var p1 = new ProjectResponseDto(1L, "P1", "D1", LocalDateTime.of(2025, 1, 1, 10, 0));
		var p2 = new ProjectResponseDto(2L, "P2", "D2", LocalDateTime.of(2025, 1, 2, 12, 30));
		when(projectService.findAllProjects()).thenReturn(List.of(p1, p2));

		mockMvc.perform(get("/api/projects"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", Matchers.hasSize(2)))
				.andExpect(jsonPath("$[0].id").value(1L))
				.andExpect(jsonPath("$[0].createdAt").value("2025-01-01T10:00:00"))
				.andExpect(jsonPath("$[1].id").value(2L))
				.andExpect(jsonPath("$[1].createdAt").value("2025-01-02T12:30:00"));
	}

	@WithMockTenantUser(roles = { Role.ROLE_ADMIN })
	@Test
	void givenAdminOnly_whenGetAllProjects_then403() throws Exception {
		mockMvc.perform(get("/api/projects"))
				.andExpect(status().isForbidden());
	}

	@Test
	void givenNoAuth_whenGetAllProjects_then401() throws Exception {
		mockMvc.perform(get("/api/projects"))
				.andExpect(status().isUnauthorized());
	}

	// ---------- GET /api/projects/{id} (MEMBER) ----------
	@WithMockTenantUser(roles = { Role.ROLE_MEMBER })
	@Test
	void givenMember_whenGetProject_then200() throws Exception {
		var resp = new ProjectResponseDto(5L, "Proj", "Desc", LocalDateTime.of(2025, 1, 3, 8, 15));
		when(projectService.findById(5L)).thenReturn(resp);

		mockMvc.perform(get("/api/projects/{id}", 5L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(5L))
				.andExpect(jsonPath("$.name").value("Proj"))
				.andExpect(jsonPath("$.createdAt").value("2025-01-03T08:15:00"));
	}

	@Test
	void givenNoAuth_whenGetProject_then401() throws Exception {
		mockMvc.perform(get("/api/projects/{id}", 5L))
				.andExpect(status().isUnauthorized());
	}

	// ---------- PATCH /api/projects/{id} (ADMIN) ----------
	@WithMockTenantUser(roles = { Role.ROLE_ADMIN })
	@Test
	void givenAdmin_whenUpdateProject_then200() throws Exception {
		String json = """
            { "name": "Updated", "description": "New Desc" }
            """;

		var resp = new ProjectResponseDto(5L, "Updated", "New Desc", LocalDateTime.of(2025, 1, 4, 9, 0));
		when(projectService.updateProject(eq(5L), any(ProjectUpdateDto.class))).thenReturn(resp);

		mockMvc.perform(patch("/api/projects/{id}", 5L)
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(5L))
				.andExpect(jsonPath("$.name").value("Updated"))
				.andExpect(jsonPath("$.createdAt").value("2025-01-04T09:00:00"));
	}

	@WithMockTenantUser(roles = { Role.ROLE_MEMBER })
	@Test
	void givenMember_whenUpdateProject_then403() throws Exception {
		String json = """
            { "name": "Updated" }
            """;

		mockMvc.perform(patch("/api/projects/{id}", 5L)
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isForbidden());
	}

	@Test
	void givenNoAuth_whenUpdateProject_then401() throws Exception {
		String json = """
            { "name": "Updated" }
            """;

		mockMvc.perform(patch("/api/projects/{id}", 5L)
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isUnauthorized());
	}

	// ---------- DELETE /api/projects/{id} (ADMIN) ----------
	@WithMockTenantUser(roles = { Role.ROLE_ADMIN })
	@Test
	void givenAdmin_whenDeleteProject_then204() throws Exception {
		mockMvc.perform(delete("/api/projects/{id}", 9L))
				.andExpect(status().isNoContent());

		verify(projectService).deleteProject(9L);
	}

	@WithMockTenantUser(roles = { Role.ROLE_MEMBER })
	@Test
	void givenMember_whenDeleteProject_then403() throws Exception {
		mockMvc.perform(delete("/api/projects/{id}", 9L))
				.andExpect(status().isForbidden());
	}

	@Test
	void givenNoAuth_whenDeleteProject_then401() throws Exception {
		mockMvc.perform(delete("/api/projects/{id}", 9L))
				.andExpect(status().isUnauthorized());
	}
}
