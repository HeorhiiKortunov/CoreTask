package com.example.maven.api.mapper;

import com.example.maven.api.dto.request.invitation.InvitationAcceptDto;
import com.example.maven.api.dto.request.invitation.InvitationCreateDto;
import com.example.maven.enums.Role;
import com.example.maven.persistence.entity.Company;
import com.example.maven.persistence.entity.Invitation;
import com.example.maven.persistence.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Component
public class InvitationMapper {
    public Invitation fromCreateDto(InvitationCreateDto dto, Company company) {
        Invitation invitation = new Invitation();
        invitation.setEmail(dto.email());
        invitation.setCompany(company);
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setExpiresAt(LocalDateTime.now().plusDays(7));
        invitation.setAccepted(false);
        return invitation;
    }

    public User fromAcceptDto(InvitationAcceptDto dto, Invitation invitation){
        User user = new User();
        user.setUsername(dto.username());
        user.setDisplayedName(dto.displayedName());
        user.setEmail(invitation.getEmail());
        user.setPassword(dto.password());
        user.setRoles(Set.of(Role.ROLE_MEMBER));

        return user;
    }
}