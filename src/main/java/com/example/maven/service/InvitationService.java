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
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Transactional
@AllArgsConstructor
public class InvitationService {
	private final InvitationRepository invitationRepository;
	private final InvitationMapper invitationMapper;
	private final CompanyRepository companyRepository;
	private final UserRepository userRepository;
	private final UserMapper userMapper;

	public void inviteUser(InvitationCreateDto dto){
		var invitation = invitationMapper.fromCreateDto(dto, companyRepository.findById(SecurityUtils.getCurrentTenantId())
				.orElseThrow(() -> new ResourceNotFoundException("Company not found")));
		invitationRepository.save(invitation);
		//TODO: email sending logic
	}

	public UserResponseDto acceptInvitation(String token, InvitationAcceptDto dto){
		Invitation invitation = invitationRepository.findByToken(token)
				.orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

		if (invitation.isAccepted() || invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw new AccessDeniedException("Invitation expired or already used");
		}

		var user = invitationMapper.fromAcceptDto(dto, invitation);
		var savedUser = userRepository.save(user);

		invitation.setAccepted(true);
		invitationRepository.save(invitation);

		return userMapper.toResponseDto(savedUser);
	}
}
