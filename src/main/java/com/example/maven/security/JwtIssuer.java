package com.example.maven.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class JwtIssuer {
	private final JwtProperties properties;

	public JwtIssuer(JwtProperties properties) {
		this.properties = properties;
	}

	public String issue(long userId, String email, List<String> roles, long companyId){
		return JWT.create()
				.withSubject(String.valueOf(userId))
				.withExpiresAt(Instant.now().plus(Duration.of(1, ChronoUnit.DAYS)))
				.withClaim("email", email)
				.withClaim("roles", roles)
				.withClaim("companyId", companyId)
				.sign(Algorithm.HMAC256(properties.getSecretKey()));
	}
}
