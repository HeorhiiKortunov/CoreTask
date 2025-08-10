package com.example.maven.api.controller;

import com.example.maven.api.dto.request.auth.LoginRequest;
import com.example.maven.api.dto.request.comment.CommentCreateDto;
import com.example.maven.api.dto.request.company.CompanyCreateDto;
import com.example.maven.api.dto.response.CommentResponseDto;
import com.example.maven.api.dto.response.CompanyResponseDto;
import com.example.maven.api.dto.response.LoginResponse;
import com.example.maven.api.dto.response.UserResponseDto;
import com.example.maven.service.AuthService;
import com.example.maven.service.CompanyService;
import com.example.maven.service.UserService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;
	private final CompanyService companyService;

	@PermitAll
	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request){
		return ResponseEntity.ok(authService.attemptLogin(request.username(), request.password()));
	}

	@PermitAll
	@PostMapping("/register-company")
	public ResponseEntity<CompanyResponseDto> registerCompany(@Valid @RequestBody CompanyCreateDto dto) {
		CompanyResponseDto responseDto = companyService.createCompany(dto);

		URI location = ServletUriComponentsBuilder
				.fromCurrentContextPath()
				.path("/api/company/{id}")
				.buildAndExpand(responseDto.id())
				.toUri();

		return ResponseEntity.created(location).body(responseDto);
	}

}