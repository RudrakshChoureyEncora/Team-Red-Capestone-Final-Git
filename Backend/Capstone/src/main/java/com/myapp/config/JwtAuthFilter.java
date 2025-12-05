
package com.myapp.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 1️⃣ Do NOT filter these APIs
        if (path.contains("/api/login") || path.contains("/api/register")) {
            chain.doFilter(request, response);
            return;
        }

        // 2️⃣ Extract Authorization header
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        // 3️⃣ Extract raw token
        String token = authHeader.substring(7);

        // ignore invalid / empty / null tokens
        if (token.trim().isEmpty() || token.equalsIgnoreCase("null")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            // 4️⃣ Extract username from token
            String username = jwtUtil.extractUsername(token);

            // 5️⃣ Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 6️⃣ Validate token & set authentication
            if (jwtUtil.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } catch (Exception ex) {
            System.out.println("JWT ERROR: " + ex.getMessage());
            // ignore token errors and continue — DO NOT break application
        }

        chain.doFilter(request, response);
    }
}
