package com.example.maven.api.dto.request.invitation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InvitationAcceptDto(
    @NotBlank String username,
    @NotBlank String displayedName,
    @NotBlank @Size(min = 6) String password
) {}