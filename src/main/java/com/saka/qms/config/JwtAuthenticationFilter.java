package com.saka.qms.config;

import com.saka.qms.model.Employee;
import com.saka.qms.repository.EmployeeRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final EmployeeRepository employeeRepository;

    public JwtAuthenticationFilter(JwtService jwtService, EmployeeRepository employeeRepository) {
        this.jwtService = jwtService;
        this.employeeRepository = employeeRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            authenticate(authorization.substring(7), request);
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(String token, HttpServletRequest request) {
        try {
            String username = jwtService.extractUsername(token);
            Boolean tokenAdmin = jwtService.extractIsAdmin(token);
            employeeRepository.findByUsername(username)
                    .filter(employee -> !Boolean.TRUE.equals(employee.getIsDeleted()))
                    .filter(employee -> jwtService.isValid(token, employee.getUsername()))
                    .ifPresent(employee -> setAuthentication(employee, tokenAdmin, request));
        } catch (JwtException | IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();
        }
    }

    private void setAuthentication(
            Employee employee,
            Boolean tokenAdmin,
            HttpServletRequest request) {
            boolean isAdmin = Boolean.TRUE.equals(employee.getIsAdmin());
            String role = isAdmin ? "ROLE_ADMIN" : "ROLE_EMPLOYEE";
            logger.debug("Authenticated username={} with role={} (dbIsAdmin={}, tokenIsAdmin={})",
                    employee.getUsername(), role, employee.getIsAdmin(), tokenAdmin);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        employee,
                        null,
                        List.of(new SimpleGrantedAuthority(role)));
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
