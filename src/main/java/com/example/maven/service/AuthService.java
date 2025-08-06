package com.example.maven.service;

import com.example.maven.api.dto.response.LoginResponse;
import com.example.maven.security.JwtIssuer;
import com.example.maven.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
	private final AuthenticationManager authenticationManager;
	private final JwtIssuer jwtIssuer;

	public LoginResponse attemptLogin(String username, String password) {
		var authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(username, password)
		);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		var principal = (UserPrincipal) authentication.getPrincipal();

		var roles = principal.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.toList();

		var token = jwtIssuer.issue(principal.getId(), principal.getUsername(), roles, principal.getTenantId());
		return new LoginResponse(token);
	}
}