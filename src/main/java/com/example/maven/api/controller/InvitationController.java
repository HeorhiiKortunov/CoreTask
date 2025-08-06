package com.example.maven.api.controller;

import com.example.maven.api.dto.request.invitation.InvitationAcceptDto;
import com.example.maven.api.dto.request.invitation.InvitationCreateDto;
import com.example.maven.api.dto.response.UserResponseDto;
import com.example.maven.service.InvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {
    private final InvitationService invitationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> inviteUser(@Valid @RequestBody InvitationCreateDto dto) {
        invitationService.inviteUser(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/accept")
    public ResponseEntity<UserResponseDto> acceptInvitation(
            @RequestParam String token,
            @Valid @RequestBody InvitationAcceptDto dto) {
        return ResponseEntity.ok(invitationService.acceptInvitation(token, dto));
    }
}
