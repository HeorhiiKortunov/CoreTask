package com.example.maven.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.maven.enums.Role;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtToPrincipalConverter {
	public UserPrincipal convert(DecodedJWT jwt){
		return UserPrincipal.builder()
				.id(Long.parseLong(jwt.getSubject()))
				.username(jwt.getClaim("email").asString())
				.tenantId(jwt.getClaim("companyId").asLong())
				.roles(extractAuthoritiesFromClaim(jwt))
				.build();

	}

	private Set<Role> extractAuthoritiesFromClaim(DecodedJWT jwt) {
		var claim = jwt.getClaim("roles");
		if (claim.isNull() || claim.isMissing()) return Set.of();
		List<String> roles = claim.asList(String.class);
		if (roles == null) return Set.of();
		return roles.stream()
				.map(Role::valueOf)
				.collect(Collectors.toSet());
	}

}
