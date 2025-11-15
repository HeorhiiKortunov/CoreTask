package com.example.maven.utils;

import com.example.maven.security.UserPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("securityUtils")
public class SecurityUtils {

	public Long getCurrentTenantId() {
		return getPrincipal().getTenantId();
	}

	public Long getCurrentUserId() {
		return getPrincipal().getId();
	}

	private UserPrincipal getPrincipal() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
			throw new AccessDeniedException("No authenticated user");
		}

		Object principal = auth.getPrincipal();
		if (principal instanceof UserPrincipal userPrincipal) {
			return userPrincipal;
		}

		throw new AccessDeniedException("Unexpected principal type: " + principal.getClass().getName());
	}
}