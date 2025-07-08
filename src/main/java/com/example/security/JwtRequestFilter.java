package com.example.security;

import com.example.dto.UserSession;
import com.example.service.CustomUserDetailsService;
import com.example.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private RedisService redisService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");
        final String requestURI = request.getRequestURI();
        
        logger.info("Processing request: " + requestURI + " with Authorization: " + 
                    (authorizationHeader != null ? "Bearer ***" : "null"));
        
        if (authorizationHeader == null) {
            logger.warn("Authorization header is NULL for request: " + requestURI);
        } else {
            logger.info("Authorization header present: " + authorizationHeader.substring(0, Math.min(authorizationHeader.length(), 20)) + "...");
        }

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtTokenUtil.extractUsername(jwt);
                logger.debug("Extracted username from JWT: " + username);
            } catch (Exception e) {
                logger.error("JWT token is invalid: " + e.getMessage());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.info("Starting JWT authentication for user: " + username);
            
            // 首先从Redis中验证token是否存在
            boolean tokenExists = redisService.isTokenExists(jwt);
            logger.info("Token exists in Redis: " + tokenExists);
            
            if (!tokenExists) {
                logger.warn("Token not found in Redis: " + jwt.substring(0, Math.min(jwt.length(), 20)) + "...");
                chain.doFilter(request, response);
                return;
            }

            // 获取用户会话信息
            UserSession userSession = redisService.getUserSession(jwt);
            logger.info("User session from Redis: " + (userSession != null ? "Found" : "Not found"));
            
            if (userSession == null) {
                logger.warn("User session not found in Redis for token: " + jwt.substring(0, Math.min(jwt.length(), 20)) + "...");
                chain.doFilter(request, response);
                return;
            }

            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                logger.info("Loaded UserDetails: " + userDetails.getClass().getSimpleName() + " - " + userDetails.getUsername());
                logger.info("User enabled: " + userDetails.isEnabled());
                logger.info("Account non expired: " + userDetails.isAccountNonExpired());
                logger.info("Account non locked: " + userDetails.isAccountNonLocked());
                logger.info("Credentials non expired: " + userDetails.isCredentialsNonExpired());

                boolean tokenValid = jwtTokenUtil.validateToken(jwt, userDetails);
                logger.info("JWT token validation result: " + tokenValid);
                
                if (tokenValid) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.info("Authentication set successfully for user: " + username + " with authorities: " + userDetails.getAuthorities());
                } else {
                    logger.warn("JWT token validation failed for user: " + username);
                }
            } catch (Exception e) {
                logger.error("Error during user authentication: " + e.getMessage(), e);
            }
        } else if (username == null && authorizationHeader != null) {
            logger.warn("Failed to extract username from JWT token");
        } else if (username != null && SecurityContextHolder.getContext().getAuthentication() != null) {
            logger.info("User " + username + " already authenticated");
        }
        
        chain.doFilter(request, response);
    }
} 