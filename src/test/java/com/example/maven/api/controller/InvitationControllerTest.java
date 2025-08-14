package com.example.maven.api.controller;

import com.example.maven.api.dto.request.invitation.InvitationAcceptDto;
import com.example.maven.api.dto.request.invitation.InvitationCreateDto;
import com.example.maven.api.dto.response.UserResponseDto;
import com.example.maven.enums.Role;
import com.example.maven.security.WebSecurityConfig;
import com.example.maven.service.InvitationService;
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

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = true)
@Import(WebSecurityConfig.class)
class InvitationControllerTest {

	@Autowired MockMvc mockMvc;
	@Autowired ObjectMapper objectMapper;

	@MockitoBean InvitationService invitationService;

	// ---------- POST /api/invitations ----------
	@WithMockTenantUser(roles = { Role.ROLE_ADMIN })
	@Test
	void givenAdmin_whenInviteUser_then200_andServiceCalled() throws Exception {
		var dto = new InvitationCreateDto("user@mail.com");

		mockMvc.perform(post("/api/invitations")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isOk());

		verify(invitationService).inviteUser(dto);
	}

	@WithMockTenantUser(roles = { Role.ROLE_MEMBER })
	@Test
	void givenMember_whenInviteUser_then403() throws Exception {
		var dto = new InvitationCreateDto("user@mail.com");

		mockMvc.perform(post("/api/invitations")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isForbidden());
	}

	@Test
	void givenNoAuth_whenInviteUser_then401() throws Exception {
		var dto = new InvitationCreateDto("user@mail.com");

		mockMvc.perform(post("/api/invitations")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isUnauthorized());
	}

	// ---------- POST /api/invitations/accept?token=... ----------
	@Test
	void givenAccept_whenValidToken_then200_andReturnsUserDto() throws Exception {
		var acceptDto = new InvitationAcceptDto("newUser", "Name", "password123");
		var resp = new UserResponseDto(
				1L,
				"newUser",
				"Name",
				"mail@mail.com",
				Set.of(Role.ROLE_MEMBER)
		);

		when(invitationService.acceptInvitation(eq("abc"), any(InvitationAcceptDto.class)))
				.thenReturn(resp);

		mockMvc.perform(post("/api/invitations/accept")
						.param("token", "abc")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(acceptDto)))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.username").value("newUser"))
				.andExpect(jsonPath("$.displayedName").value("Name"))
				.andExpect(jsonPath("$.email").value("mail@mail.com"))
				.andExpect(jsonPath("$.roles", Matchers.hasSize(1)));
	}
}
