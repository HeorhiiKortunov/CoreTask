package com.example.maven.api.controller;

import com.example.maven.api.dto.request.user.UserUpdateDto;
import com.example.maven.api.dto.request.user.UserUpdateRolesDto;
import com.example.maven.api.dto.response.UserResponseDto;
import com.example.maven.enums.Role;
import com.example.maven.security.WebSecurityConfig;
import com.example.maven.service.UserService;
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
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные MVC-тесты для UserController с реальной SecurityFilterChain.
 * CSRF у тебя выключен, поэтому без .with(csrf()).
 * Роли/пользователь подменяются через @WithMockTenantUser.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = true)
@Import(WebSecurityConfig.class)
class UserControllerTest {

	@Autowired MockMvc mockMvc;
	@Autowired ObjectMapper objectMapper;

	@MockitoBean UserService userService;

	// ---------- GET /api/users ----------
	@WithMockTenantUser(roles = { Role.ROLE_MEMBER })
	@Test
	void givenMember_whenGetAllUsers_then200AndListReturned() throws Exception {
		var u1 = new UserResponseDto(1L, "u1", "U1", "u1@mail.com", Set.of(Role.ROLE_MEMBER));
		var u2 = new UserResponseDto(2L, "u2", "U2", "u2@mail.com", Set.of(Role.ROLE_MEMBER));
		when(userService.findCompanyUsers()).thenReturn(List.of(u1, u2));

		mockMvc.perform(get("/api/users"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", Matchers.hasSize(2)))
				.andExpect(jsonPath("$[0].id").value(1L))
				.andExpect(jsonPath("$[1].id").value(2L));
	}

	@WithMockTenantUser(roles = { Role.ROLE_ADMIN }) // нет ROLE_MEMBER => 403
	@Test
	void givenAdminOnly_whenGetAllUsers_then403() throws Exception {
		mockMvc.perform(get("/api/users"))
				.andExpect(status().isForbidden());
	}

	@Test
	void givenNoAuth_whenGetAllUsers_then401() throws Exception {
		mockMvc.perform(get("/api/users"))
				.andExpect(status().isUnauthorized());
	}

	// ---------- GET /api/users/me ----------
	@WithMockTenantUser(userId = 10L, roles = { Role.ROLE_MEMBER })
	@Test
	void givenMember_whenGetMe_then200AndProfileReturned() throws Exception {
		var me = new UserResponseDto(10L, "me", "Me", "me@mail.com", Set.of(Role.ROLE_MEMBER));
		when(userService.findById(10L)).thenReturn(me);

		mockMvc.perform(get("/api/users/me"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(10L))
				.andExpect(jsonPath("$.username").value("me"))
				.andExpect(jsonPath("$.displayedName").value("Me"))
				.andExpect(jsonPath("$.email").value("me@mail.com"));
	}

	@Test
	void givenNoAuth_whenGetMe_then401() throws Exception {
		mockMvc.perform(get("/api/users/me"))
				.andExpect(status().isUnauthorized());
	}

	// ---------- GET /api/users/{id} ----------
	@WithMockTenantUser(roles = { Role.ROLE_MEMBER })
	@Test
	void givenMember_whenGetUserById_then200() throws Exception {
		var u = new UserResponseDto(5L, "john", "John", "john@mail.com", Set.of(Role.ROLE_MEMBER));
		when(userService.findById(5L)).thenReturn(u);

		mockMvc.perform(get("/api/users/{id}", 5L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(5L))
				.andExpect(jsonPath("$.username").value("john"))
				.andExpect(jsonPath("$.displayedName").value("John"))
				.andExpect(jsonPath("$.email").value("john@mail.com"));
	}

	@Test
	void givenNoAuth_whenGetUserById_then401() throws Exception {
		mockMvc.perform(get("/api/users/{id}", 5L))
				.andExpect(status().isUnauthorized());
	}

	// ---------- PUT /api/users/roles/{id} ----------
	@WithMockTenantUser(roles = { Role.ROLE_ADMIN })
	@Test
	void givenAdmin_whenUpdateUserRoles_then200AndReturnedBody() throws Exception {
		var req = new UserUpdateRolesDto(Set.of(Role.ROLE_MEMBER, Role.ROLE_ADMIN));
		var resp = new UserResponseDto(7L, "alice", "Alice", "alice@mail.com",
				Set.of(Role.ROLE_MEMBER, Role.ROLE_ADMIN));
		when(userService.updateUserRolesById(eq(7L), any(UserUpdateRolesDto.class))).thenReturn(resp);

		mockMvc.perform(put("/api/users/roles/{id}", 7L)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(7L))
				.andExpect(jsonPath("$.roles", Matchers.hasSize(2)));
	}

	@WithMockTenantUser(roles = { Role.ROLE_MEMBER })
	@Test
	void givenMember_whenUpdateUserRoles_then403() throws Exception {
		var req = new UserUpdateRolesDto(Set.of(Role.ROLE_MEMBER));

		mockMvc.perform(put("/api/users/roles/{id}", 7L)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isForbidden());
	}

	@Test
	void givenNoAuth_whenUpdateUserRoles_then401() throws Exception {
		var req = new UserUpdateRolesDto(Set.of(Role.ROLE_MEMBER));

		mockMvc.perform(put("/api/users/roles/{id}", 7L)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isUnauthorized());
	}

	// ---------- PATCH /api/users/me (меняет роли текущего) ----------
	@WithMockTenantUser(userId = 10L, roles = { Role.ROLE_MEMBER })
	@Test
	void givenMember_whenPatchMeRoles_then200() throws Exception {
		var req = new UserUpdateRolesDto(Set.of(Role.ROLE_MEMBER));
		var resp = new UserResponseDto(10L, "bob", "Bob", "b@mail.com", Set.of(Role.ROLE_MEMBER));
		when(userService.updateUserRolesById(eq(10L), any(UserUpdateRolesDto.class))).thenReturn(resp);

		mockMvc.perform(patch("/api/users/me")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(10L))
				.andExpect(jsonPath("$.displayedName").value("Bob"))
				.andExpect(jsonPath("$.email").value("b@mail.com"));
	}

	@Test
	void givenNoAuth_whenPatchMeRoles_then401() throws Exception {
		var req = new UserUpdateRolesDto(Set.of(Role.ROLE_MEMBER));

		mockMvc.perform(patch("/api/users/me")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(req)))
				.andExpect(status().isUnauthorized());
	}

	// ---------- PUT /api/users/{id} (UserUpdateDto: displayedName + email) ----------
	@WithMockTenantUser(roles = { Role.ROLE_ADMIN })
	@Test
	void givenAdmin_whenUpdateUser_withValidEmail_then200() throws Exception {
		var req = new UserUpdateDto();
		// setter-ы у Lombok @Getter нет — поэтому соберём JSON строкой
		String json = """
                {
                  "displayedName": "New Name",
                  "email": "new@mail.com"
                }
                """;

		var resp = new UserResponseDto(15L, "jane", "New Name", "new@mail.com", Set.of(Role.ROLE_MEMBER));
		when(userService.updateUser(eq(15L), any(UserUpdateDto.class))).thenReturn(resp);

		mockMvc.perform(put("/api/users/{id}", 15L)
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(15L))
				.andExpect(jsonPath("$.displayedName").value("New Name"))
				.andExpect(jsonPath("$.email").value("new@mail.com"));
	}

	@WithMockTenantUser(roles = { Role.ROLE_ADMIN })
	@Test
	void givenAdmin_whenUpdateUser_withInvalidEmail_then400() throws Exception {
		// Работает только если в контроллере стоит @Valid на @RequestBody UserUpdateDto
		String json = """
                {
                  "displayedName": "Any",
                  "email": "not-an-email"
                }
                """;

		mockMvc.perform(put("/api/users/{id}", 15L)
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isBadRequest());
	}

	@WithMockTenantUser(roles = { Role.ROLE_MEMBER })
	@Test
	void givenMember_whenUpdateUser_then403() throws Exception {
		String json = """
                {
                  "displayedName": "X",
                  "email": "x@mail.com"
                }
                """;

		mockMvc.perform(put("/api/users/{id}", 15L)
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isForbidden());
	}

	@Test
	void givenNoAuth_whenUpdateUser_then401() throws Exception {
		String json = """
                {
                  "displayedName": "X",
                  "email": "x@mail.com"
                }
                """;

		mockMvc.perform(put("/api/users/{id}", 15L)
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isUnauthorized());
	}

	// ---------- DELETE /api/users/{id} ----------
	@WithMockTenantUser(roles = { Role.ROLE_ADMIN })
	@Test
	void givenAdmin_whenDeleteUser_then204() throws Exception {
		mockMvc.perform(delete("/api/users/{id}", 9L))
				.andExpect(status().isNoContent());

		verify(userService).deleteUser(9L);
	}

	@WithMockTenantUser(roles = { Role.ROLE_MEMBER })
	@Test
	void givenMember_whenDeleteUser_then403() throws Exception {
		mockMvc.perform(delete("/api/users/{id}", 9L))
				.andExpect(status().isForbidden());
	}

	@Test
	void givenNoAuth_whenDeleteUser_then401() throws Exception {
		mockMvc.perform(delete("/api/users/{id}", 9L))
				.andExpect(status().isUnauthorized());
	}

	// ---------- DELETE /api/users/me ----------
	@WithMockTenantUser(userId = 33L, roles = { Role.ROLE_MEMBER })
	@Test
	void givenMember_whenDeleteMe_then204() throws Exception {
		mockMvc.perform(delete("/api/users/me"))
				.andExpect(status().isNoContent());

		verify(userService).deleteUser(33L);
	}

	@Test
	void givenNoAuth_whenDeleteMe_then401() throws Exception {
		mockMvc.perform(delete("/api/users/me"))
				.andExpect(status().isUnauthorized());
	}
}
