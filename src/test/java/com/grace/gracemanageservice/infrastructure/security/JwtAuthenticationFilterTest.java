package com.grace.gracemanageservice.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link JwtAuthenticationFilter}.
 * Tests the filter behavior in isolation without starting the Spring context.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void protectedEndpoint_withoutAuthorizationHeader_doesNotAuthenticate() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/__test/whoami");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Should continue the filter chain without authentication
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtTokenProvider, userDetailsService);
    }

    @Test
    void protectedEndpoint_withNonBearerAuthorizationHeader_doesNotAuthenticate() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/__test/whoami");
        request.addHeader("Authorization", "Basic abc");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtTokenProvider, userDetailsService);
    }

    @Test
    void protectedEndpoint_withExpiredBearerToken_throwsBadCredentialsException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/__test/whoami");
        String token = "expired-token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtTokenProvider.isTokenExpired(token)).thenReturn(true);

        // Filter throws BadCredentialsException for expired tokens
        assertThatThrownBy(() -> jwtAuthenticationFilter.doFilterInternal(request, response, filterChain))
            .isInstanceOf(org.springframework.security.authentication.BadCredentialsException.class)
            .hasMessage("Expired JWT token");

        // Filter should NOT call filterChain (returns early due to exception)
        verifyNoInteractions(filterChain);
        verify(jwtTokenProvider).isTokenExpired(token);
        verify(jwtTokenProvider, never()).validateAccessToken(anyString());
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void protectedEndpoint_withValidAccessToken_setsSecurityContext() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/__test/whoami");
        String token = "valid-token";
        String username = "alice";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtTokenProvider.isTokenExpired(token)).thenReturn(false);
        when(jwtTokenProvider.validateAccessToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(token)).thenReturn(username);

        UserDetails userDetails = new User(
            username,
            "n/a",
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(username);

        verify(jwtTokenProvider).isTokenExpired(token);
        verify(jwtTokenProvider).validateAccessToken(token);
        verify(jwtTokenProvider).getUsernameFromToken(token);
        verify(userDetailsService).loadUserByUsername(username);
    }

    @Test
    void protectedEndpoint_withNonAccessToken_doesNotAuthenticate() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/__test/whoami");
        String token = "refresh-or-invalid";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtTokenProvider.isTokenExpired(token)).thenReturn(false);
        when(jwtTokenProvider.validateAccessToken(token)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verify(jwtTokenProvider).isTokenExpired(token);
        verify(jwtTokenProvider).validateAccessToken(token);
        verify(jwtTokenProvider, never()).getUsernameFromToken(anyString());
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void authEndpoint_isNotFiltered() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/auth/login");
        request.setRequestURI("/api/v1/auth/login");
        String token = "expired-token";
        request.addHeader("Authorization", "Bearer " + token);

        // shouldNotFilter returns true for auth endpoints
        boolean shouldSkip = jwtAuthenticationFilter.shouldNotFilter(request);
        assertThat(shouldSkip).isTrue();

        verifyNoInteractions(jwtTokenProvider, userDetailsService);
    }
}

