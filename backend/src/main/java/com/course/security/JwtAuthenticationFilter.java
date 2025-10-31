package com.course.security;

import com.course.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/auth/login",
            "/auth/register",
            "/auth/refresh",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/files/**",
            "/error"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        log.debug("Request path: {}", path);
        
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }
        
        if (path.startsWith("/public/")) {
            return true;
        }
        
        boolean shouldExclude = EXCLUDE_PATHS.stream()
                .anyMatch(excludePath -> path.startsWith(excludePath) || path.matches(excludePath.replace("**", ".*")));
        
        if (shouldExclude) {
            log.debug("Excluding path from JWT filter: {}", path);
        }
        
        return shouldExclude;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = extractToken(request);
            log.debug("Extracted JWT token: {}", token);
            
            if (token != null) {
                String username = jwtTokenUtil.extractUsername(token);
                log.debug("Successfully set authentication for user: {}", username);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                if (jwtTokenUtil.validateToken(token, userDetails)) {
                    if (setAuthentication(username, request)) {
                        filterChain.doFilter(request, response);
                        return;
                    }
                } else {
                    handleAuthenticationError(response, "Invalid token");
                    return;
                }
            }
            
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage());
            handleAuthenticationError(response, e.getMessage());
        }
    }

    private boolean setAuthentication(String username, HttpServletRequest request) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (userDetails != null && userDetails.isEnabled()) {
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + userDetails.getAuthorities().iterator().next().getAuthority()));
                
                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("Successfully set authentication for user: {}", username);
                return true;
            }
        } catch (Exception e) {
            log.error("Error setting authentication for user {}: {}", username, e.getMessage());
        }
        return false;
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            log.debug("Extracted JWT token: {}", maskToken(token));
            return token;
        }
        return null;
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "***";
        }
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }

    private void handleAuthenticationError(HttpServletResponse response, String message) throws IOException {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", new Date());
        errorDetails.put("status", HttpStatus.UNAUTHORIZED.value());
        errorDetails.put("error", "Unauthorized");
        errorDetails.put("message", message);

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), errorDetails);
    }
}
