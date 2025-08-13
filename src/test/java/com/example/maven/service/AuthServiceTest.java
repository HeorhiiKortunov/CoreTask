package com.example.maven.service;

import com.example.maven.api.dto.response.LoginResponse;
import com.example.maven.security.JwtIssuer;
import com.example.maven.security.UserPrincipal;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtIssuer jwtIssuer;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void clearContextBefore() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void clearContextAfter() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void givenValidCredentials_whenAttemptLogin_thenJwtReturned_andSecurityContextSet() {
        String username = "john";
        String rawPassword = "p@ss";
        long userId = 42L;
        Long tenantId = 7L;

        GrantedAuthority a1 = () -> "ROLE_USER";
        GrantedAuthority a2 = () -> "ROLE_ADMIN";
        List<GrantedAuthority> authorities = List.of(a1, a2);

        UserPrincipal principal = mock(UserPrincipal.class);
        when(principal.getId()).thenReturn(userId);
        when(principal.getUsername()).thenReturn(username);
        when(principal.getTenantId()).thenReturn(tenantId);
        doReturn(authorities).when(principal).getAuthorities();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        String issuedToken = "jwt-token-123";
        when(jwtIssuer.issue(userId, username, List.of("ROLE_USER", "ROLE_ADMIN"), tenantId))
                .thenReturn(issuedToken);

        LoginResponse response = authService.attemptLogin(username, rawPassword);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo(issuedToken);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(authentication);

        verify(jwtIssuer).issue(eq(userId), eq(username),
                eq(List.of("ROLE_USER", "ROLE_ADMIN")), eq(tenantId));

        ArgumentCaptor<UsernamePasswordAuthenticationToken> tokenCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(tokenCaptor.capture());
        UsernamePasswordAuthenticationToken usedToken = tokenCaptor.getValue();
        assertThat(usedToken.getPrincipal()).isEqualTo(username);
        assertThat(usedToken.getCredentials()).isEqualTo(rawPassword);
    }

    @Test
    void givenBadCredentials_whenAttemptLogin_thenExceptionPropagated_andSecurityContextNotSet() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.attemptLogin("john", "wrong"))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Bad credentials");

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verifyNoInteractions(jwtIssuer);
    }
}
