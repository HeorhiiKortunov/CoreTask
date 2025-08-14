package com.example.maven.api.controller;

import com.example.maven.api.dto.request.comment.CommentCreateDto;
import com.example.maven.api.dto.request.comment.CommentUpdateDto;
import com.example.maven.api.dto.response.CommentResponseDto;
import com.example.maven.enums.Role;
import com.example.maven.security.WebSecurityConfig;
import com.example.maven.service.CommentService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = true)
@Import(WebSecurityConfig.class)
class CommentControllerTest {

	@Autowired MockMvc mockMvc;
	@Autowired ObjectMapper objectMapper;

	@MockitoBean CommentService commentService;

	// ---------- POST /api/comments (MEMBER) ----------
	@WithMockTenantUser(roles = { Role.ROLE_MEMBER })
	@Test
	void givenMember_whenCreateComment_then200() throws Exception {
		String json = """
            { "taskId": 7, "contents": "Nice one!" }
            """;

		var resp = new CommentResponseDto(1L, 7L, 11L, "Nice one!");
		when(commentService.createComment(any(CommentCreateDto.class))).thenReturn(resp);

		mockMvc.perform(post("/api/comments")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.taskId").value(7L))
				.andExpect(jsonPath("$.authorId").value(11L))
				.andExpect(jsonPath("$.contents").value("Nice one!"));
	}

	@Test
	void givenNoAuth_whenCreateComment_then401() throws Exception {
		String json = """
            { "taskId": 7, "contents": "Nice one!" }
            """;

		mockMvc.perform(post("/api/comments")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isUnauthorized());
	}

	// ---------- GET /api/comments/{id} (MEMBER) ----------
	@WithMockTenantUser(roles = { Role.ROLE_MEMBER })
	@Test
	void givenMember_whenGetCommentById_then200() throws Exception {
		var resp = new CommentResponseDto(3L, 7L, 11L, "Hello");
		when(commentService.findCommentById(3L)).thenReturn(resp);

		mockMvc.perform(get("/api/comments/{id}", 3L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(3L))
				.andExpect(jsonPath("$.taskId").value(7L))
				.andExpect(jsonPath("$.authorId").value(11L))
				.andExpect(jsonPath("$.contents").value("Hello"));
	}

	@Test
	void givenNoAuth_whenGetCommentById_then401() throws Exception {
		mockMvc.perform(get("/api/comments/{id}", 3L))
				.andExpect(status().isUnauthorized());
	}

	// ---------- GET /api/comments?taskId=... (MEMBER) ----------
	@WithMockTenantUser(roles = { Role.ROLE_MEMBER })
	@Test
	void givenMember_whenGetAllByTask_then200AndList() throws Exception {
		var c1 = new CommentResponseDto(1L, 7L, 11L, "A");
		var c2 = new CommentResponseDto(2L, 7L, 12L, "B");
		when(commentService.findAllCommentsByTaskId(7L)).thenReturn(List.of(c1, c2));

		mockMvc.perform(get("/api/comments").param("taskId", "7"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", Matchers.hasSize(2)))
				.andExpect(jsonPath("$[0].id").value(1L))
				.andExpect(jsonPath("$[0].contents").value("A"))
				.andExpect(jsonPath("$[1].id").value(2L))
				.andExpect(jsonPath("$[1].contents").value("B"));
	}

	@Test
	void givenNoAuth_whenGetAllByTask_then401() throws Exception {
		mockMvc.perform(get("/api/comments").param("taskId", "7"))
				.andExpect(status().isUnauthorized());
	}

	// ---------- PUT /api/comments/{id} (MEMBER) ----------
	@WithMockTenantUser(roles = { Role.ROLE_MEMBER })
	@Test
	void givenMember_whenUpdateMyComment_then200() throws Exception {
		String json = """
            { "contents": "Edited!" }
            """;
		var updated = new CommentResponseDto(5L, 7L, 11L, "Edited!");
		when(commentService.updateMyCommentById(eq(5L), any(CommentUpdateDto.class))).thenReturn(updated);

		mockMvc.perform(put("/api/comments/{id}", 5L)
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(5L))
				.andExpect(jsonPath("$.contents").value("Edited!"));
	}

	@Test
	void givenNoAuth_whenUpdateMyComment_then401() throws Exception {
		String json = """
            { "contents": "Edited!" }
            """;

		mockMvc.perform(put("/api/comments/{id}", 5L)
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isUnauthorized());
	}

	// ---------- DELETE /api/comments/{id} (MEMBER) ----------
	@WithMockTenantUser(roles = { Role.ROLE_MEMBER })
	@Test
	void givenMember_whenDeleteMyComment_then204() throws Exception {
		mockMvc.perform(delete("/api/comments/{id}", 9L))
				.andExpect(status().isNoContent());

		verify(commentService).deleteMyCommentById(9L);
	}

	@Test
	void givenNoAuth_whenDeleteMyComment_then401() throws Exception {
		mockMvc.perform(delete("/api/comments/{id}", 9L))
				.andExpect(status().isUnauthorized());
	}
}
