package com.keepcalm.placementportal.filter;

import com.keepcalm.placementportal.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import com.keepcalm.placementportal.entity.User;
import com.keepcalm.placementportal.repository.UserRepository;
import com.keepcalm.placementportal.security.PortalPrincipal;
import com.keepcalm.placementportal.security.AccessSessionStore;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final AccessSessionStore accessSessions;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            if (!jwtUtil.isTokenExpired(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                Long userId = jwtUtil.extractUserId(token);
                User user = userId == null
                        ? userRepository.findByUsername(jwtUtil.extractUsername(token)).orElse(null)
                        : userRepository.findById(userId).orElse(null);
                String jti = jwtUtil.extractJti(token);
                if (user != null && Boolean.TRUE.equals(user.getIsActive()) && jti != null
                        && accessSessions.isActive(user.getInstitution().getId(), user.getId(), jti)) {
                    List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
                    PortalPrincipal principal = new PortalPrincipal(user.getId(), user.getInstitution().getId(), user.getUsername(), user.getRole());
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (RuntimeException ignored) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
