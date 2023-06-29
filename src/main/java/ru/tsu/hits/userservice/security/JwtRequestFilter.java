package ru.tsu.hits.userservice.security;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.tsu.hits.userservice.exception.JwtTokenExpiredException;
import ru.tsu.hits.userservice.exception.JwtTokenMalformedException;
import ru.tsu.hits.userservice.exception.JwtTokenMissingException;
import ru.tsu.hits.userservice.service.CustomUserDetailsService;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    private static final List<String> EXCLUDED_PATHS = Arrays.asList("/api/validate","/api/authenticate", "/api/users/sign-up", "/swagger-ui", "/v3/api-docs", "/swagger-ui.html", "/webjars", "/v2", "/swagger-resources");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Skip JWT extraction and validation for excluded paths
        if (EXCLUDED_PATHS.stream().noneMatch(path::startsWith)) {

            final String requestTokenHeader = request.getHeader("Authorization");

            if (!"OPTIONS".equalsIgnoreCase(request.getMethod())) {
                String username = null;
                String jwtToken = null;

                if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
                    jwtToken = requestTokenHeader.substring(7);
                    try {
                        username = jwtUtil.extractUsername(jwtToken);
                    } catch (IllegalArgumentException e) {
                        throw new JwtTokenMalformedException("Unable to get JWT Token");
                    } catch (ExpiredJwtException e) {
                        throw new JwtTokenExpiredException("JWT Token has expired");
                    }
                } else {
                    throw new JwtTokenMissingException("JWT Token does not begin with Bearer String");
                }

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                    if (jwtUtil.validateToken(jwtToken, userDetails)) {
                        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        usernamePasswordAuthenticationToken
                                .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    }
                }
            }
        }

        chain.doFilter(request, response);
    }
}

