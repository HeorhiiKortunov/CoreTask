package com.example.maven.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtDecoder jwtDecoder;
	private final JwtToPrincipalConverter jwtToPrincipalConverter;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		String path = request.getRequestURI();
		System.out.println("Processing request for path: " + path);

		if (path.startsWith("/api/auth/") || path.equals("/api/invitations/accept")) {
			System.out.println("Skipping JWT for public path: " + path);
			filterChain.doFilter(request, response);
			return;
		}

		try {
			extractTokenFromRequest(request)
					.map(jwtDecoder::decode)
					.map(jwtToPrincipalConverter::convert)
					.map(UserPrincipalAuthToken::new)
					.ifPresent(authentication -> SecurityContextHolder.getContext().setAuthentication(authentication));
		} catch (Exception ex) {
			System.out.println("EXCEPTION IN SECURITY");
		}

		filterChain.doFilter(request, response);
	}


	private Optional<String> extractTokenFromRequest(HttpServletRequest request){
		var token = request.getHeader("Authorization");
		if (StringUtils.hasText(token) && token.startsWith("Bearer ")){
			return Optional.of(token.substring(7));
		}
		return Optional.empty();
	}
}
