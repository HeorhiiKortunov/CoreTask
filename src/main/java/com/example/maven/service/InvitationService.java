package com.example.maven.service;

import com.example.maven.api.dto.request.invitation.InvitationAcceptDto;
import com.example.maven.api.dto.request.invitation.InvitationCreateDto;
import com.example.maven.api.dto.response.UserResponseDto;
import com.example.maven.api.mapper.InvitationMapper;
import com.example.maven.api.mapper.UserMapper;
import com.example.maven.exception.ResourceNotFoundException;
import com.example.maven.persistence.entity.Invitation;
import com.example.maven.persistence.repository.CompanyRepository;
import com.example.maven.persistence.repository.InvitationRepository;
import com.example.maven.persistence.repository.UserRepository;
import com.example.maven.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class InvitationService {
	private final InvitationRepository invitationRepository;
	private final InvitationMapper invitationMapper;
	private final CompanyRepository companyRepository;
	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final EmailService emailService;
	private final PasswordEncoder passwordEncoder;

	@Value("${app.public-base-url:http://localhost:8080}")
	private String publicBaseUrl;

	public void inviteUser(InvitationCreateDto dto) {
		var company = companyRepository.findById(SecurityUtils.getCurrentTenantId())
				.orElseThrow(() -> new ResourceNotFoundException("Company not found"));

		var invitation = invitationMapper.fromCreateDto(dto, company);
		invitation.setToken(UUID.randomUUID().toString());
		invitation.setExpiresAt(LocalDateTime.now().plusDays(2));
		invitationRepository.save(invitation);

		String inviteLink = UriComponentsBuilder
				.fromHttpUrl(publicBaseUrl)
				.path("/api/invitations/accept")
				.queryParam("token", invitation.getToken())
				.toUriString();

		emailService.sendInvitationEmail(
				invitation.getEmail(),
				"Invitation to join company",
				"""
				<p>You were invited to join our company.</p>
				<p><a href="%s">Click here to accept</a></p>
				<p>The link is available for 48 hours.</p>
				""".formatted(inviteLink)
		);
	}


	public UserResponseDto acceptInvitation(String token, InvitationAcceptDto dto){
		Invitation invitation = invitationRepository.findByToken(token)
				.orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

		if (invitation.isAccepted() || invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new AccessDeniedException("Invitation expired or already used");
		}

		var user = invitationMapper.fromAcceptDto(dto, invitation);
		user.setPassword(passwordEncoder.encode(dto.password()));
		var savedUser = userRepository.save(user);

		invitation.setAccepted(true);
		invitationRepository.save(invitation);

		return userMapper.toResponseDto(savedUser);
	}
}
