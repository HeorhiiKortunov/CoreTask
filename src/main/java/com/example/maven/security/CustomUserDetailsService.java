package com.example.maven.security;

import com.example.maven.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) {
		var user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

		Long tenantId = (user.getCompany() != null) ? user.getCompany().getId() : null;

		return new UserPrincipal(
				user.getId(),
				user.getUsername(),
				user.getPassword(),
				tenantId,
				user.getRoles()
		);
	}
}
