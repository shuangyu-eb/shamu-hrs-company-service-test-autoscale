package com.tardisone.companyservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class JWTAuthorizationFilter implements Filter {

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String authHeader = ((HttpServletRequest)servletRequest).getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String authToken = authHeader.substring(7);
            Authentication resultAuthentication = jwtTokenProvider.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(resultAuthentication);
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
