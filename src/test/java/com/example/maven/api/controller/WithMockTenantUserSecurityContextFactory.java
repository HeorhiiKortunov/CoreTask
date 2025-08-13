package com.example.maven.api.controller;

import com.example.maven.enums.Role;
import com.example.maven.security.UserPrincipal;
import com.example.maven.security.UserPrincipalAuthToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class WithMockTenantUserSecurityContextFactory
		implements WithSecurityContextFactory<WithMockTenantUser> {

	@Override
	public SecurityContext createSecurityContext(WithMockTenantUser a) {
		Set<Role> roles = new HashSet<>(Arrays.asList(a.roles()));

		var principal = UserPrincipal.builder()
				.id(a.userId())
				.username(a.username())
				.tenantId(a.tenantId())
				.roles(roles)
				.build();

		var context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(new UserPrincipalAuthToken(principal));
		return context;
	}
}
