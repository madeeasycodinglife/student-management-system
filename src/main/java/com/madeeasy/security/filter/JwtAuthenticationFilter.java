package com.madeeasy.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.madeeasy.entity.Token;
import com.madeeasy.entity.User;
import com.madeeasy.exception.TokenException;
import com.madeeasy.exception.TokenValidationException;
import com.madeeasy.repository.TokenRepository;
import com.madeeasy.repository.UserRepository;
import com.madeeasy.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {


        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!authorizationHeader.startsWith("Bearer ")) {
            handleInvalidToken(response, "Authorization header missing or malformed.");
            return; // Exit the filter chain
        }

        String accessToken = authorizationHeader.substring(7);
        String userName = null;

        try {
            userName = jwtUtils.getUserName(accessToken);
        } catch (TokenValidationException e) {
            handleInvalidToken(response, e.getMessage());
            return; // Exit the filter chain
        }
        User user = userRepository.findByEmail(userName)
                .orElse(null);
        Token token = tokenRepository.findByToken(accessToken)
                .orElse(null);
        if (token == null || user == null) {
            handleInvalidToken(response, "Token Not Found or Invalid Token !!");
            return;
        }
        try {
            if (token.isExpired() || token.isRevoked()) {
                throw new TokenException("Token is expired or revoked");
            }
        } catch (TokenException e) {
            handleInvalidToken(response, e.getMessage());
            return; // Exit the filter chain
        }

        if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtils.validateToken(accessToken, userName) &&
                    user.isAccountNonExpired() &&
                    user.isAccountNonLocked() &&
                    user.isCredentialsNonExpired() &&
                    user.isEnabled()) {

                List<SimpleGrantedAuthority> authorities = jwtUtils.getRolesFromToken(accessToken)
                        .stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userName, null, authorities);

                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(request, response);
    }


    private void handleInvalidToken(HttpServletResponse response, String message) throws IOException {
        Map<String, Object> errorResponse = Map.of(
                "status", HttpStatus.UNAUTHORIZED,
                "message", message
        );

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
