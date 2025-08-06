package com.example.maven.api.controller;

import com.example.maven.api.dto.request.auth.LoginRequest;
import com.example.maven.api.dto.response.LoginResponse;
import com.example.maven.service.AuthService;
import com.example.maven.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;
	private final UserService userService;

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@RequestBody @Validated LoginRequest request){
		return ResponseEntity.ok(authService.attemptLogin(request.username(), request.password()));
	}



}