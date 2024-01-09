package com.alibou.security.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

  private final HandlerExceptionResolver handlerExceptionResolver;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
          throws ServletException, IOException {



    if (shouldSkipAuthentication(request)) {
      filterChain.doFilter(request, response);
      return;
    }

    var authentication = authenticate(request);
    if (authentication != null) {
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }

  private boolean shouldSkipAuthentication(HttpServletRequest request) {
    return request.getServletPath().contains("/api/v1/auth") || !hasValidJwtHeader(request);
  }

  private boolean hasValidJwtHeader(HttpServletRequest request) {
    return extractJwt(request) != null;
  }

  private UsernamePasswordAuthenticationToken authenticate(HttpServletRequest request) {
    var jwt = extractJwt(request);
    var userEmail = jwtService.extractUsername(jwt);
    if (userEmail != null && isValidJwtAndUser(jwt, userEmail)) {
      var userDetails = userDetailsService.loadUserByUsername(userEmail);
      return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
    return null;
  }

  private String extractJwt(HttpServletRequest request) {

    var authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      return authHeader.substring(7);
    } else {
      return null;
    }
  }

  private boolean isValidJwtAndUser(String jwt, String userEmail) {
    var userDetails = userDetailsService.loadUserByUsername(userEmail);
    return jwtService.isTokenValid(jwt, userDetails);
  }

}