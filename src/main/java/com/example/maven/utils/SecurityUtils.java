package com.example.maven.utils;

import com.example.maven.security.UserPrincipal;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
	public static Long getCurrentTenantId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.getPrincipal() instanceof UserPrincipal principal) {
			return principal.getTenantId();
		}
		throw new AccessDeniedException("No tenant info");
	}
}
