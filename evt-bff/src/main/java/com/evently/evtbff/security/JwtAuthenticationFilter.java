
package com.evently.evtbff.security;

import com.evently.evtbff.service.auth.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        if (!jwtService.isTokenValid(token)) {
            writeUnauthorized(response);
            return;
        }

        Claims claims = jwtService.extractClaims(token);

        // Refresh tokens must not authenticate API requests.
        if ("refresh".equals(claims.get("type", String.class))) {
            writeUnauthorized(response);
            return;
        }

        String userId = claims.get("userId", String.class);
        String role = claims.get("role", String.class);

        if (userId == null || role == null) {
            writeUnauthorized(response);
            return;
        }

        var authentication =
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_" + role)
                        )
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        request.setAttribute("userId", userId);
        request.setAttribute("role", role);

        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(
            HttpServletResponse response
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        response.getWriter().write(
                "{\"success\":false,\"message\":\"Invalid or expired access token\"}"
        );
    }
}
