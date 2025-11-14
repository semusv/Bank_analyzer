package ru.vvsem.bank.analyzer.components.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.vvsem.bank.analyzer.services.security.CustomUserDetailsService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService customUserDetailsService;

    private final JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        String jwtToken = extractJwtToken(request);

        if (jwtToken != null) {
            processJwtToken(jwtToken, request, response);
        }

        chain.doFilter(request, response);
    }

    private String extractJwtToken(HttpServletRequest request) {
        String token = getTokenFromHeader(request);
        return token != null ? token : getTokenFromCookie(request);
    }

    private String getTokenFromHeader(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private String getTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("JWT_TOKEN".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void processJwtToken(
            String jwtToken, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String username = extractUsernameFromToken(jwtToken);
            if (username != null && isAuthenticationNotSet()) {
                authenticateUser(username, jwtToken, request);
            }
        } catch (ExpiredJwtException e) {
            handleExpiredToken(response);
        } catch (Exception e) {
            logger.warn("JWT token processing failed: " + e.getMessage());
        }
    }

    private String extractUsernameFromToken(String jwtToken) {
        try {
            return jwtTokenUtil.extractUsername(jwtToken);
        } catch (IllegalArgumentException e) {
            logger.warn("Unable to get JWT Token");
            return null;
        }
    }

    private boolean isAuthenticationNotSet() {
        return SecurityContextHolder.getContext().getAuthentication() == null;
    }

    private void authenticateUser(String username, String jwtToken, HttpServletRequest request) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        if (isTokenValid(jwtToken, userDetails)) {
            setAuthenticationInContext(userDetails, request);
        }

    }

    private boolean isTokenValid(String jwtToken, UserDetails userDetails) {
        return Boolean.TRUE.equals(jwtTokenUtil.validateToken(jwtToken, userDetails));
    }

    private void setAuthenticationInContext(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private void handleExpiredToken(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("JWT token expired");
        logger.warn("JWT Token has expired");
    }

}