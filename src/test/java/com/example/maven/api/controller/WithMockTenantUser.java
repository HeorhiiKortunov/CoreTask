package com.example.maven.api.controller;

import com.example.maven.enums.Role;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockTenantUserSecurityContextFactory.class)
public @interface WithMockTenantUser {
	long userId() default 1L;
	long tenantId() default 1L;
	String username() default "John";
	Role[] roles() default { Role.ROLE_MEMBER };
}