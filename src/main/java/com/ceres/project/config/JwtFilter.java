package com.ceres.project.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.ceres.project.models.database.SystemUserModel;
import com.ceres.project.utils.OperationReturnObject;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtility jwtUtility;
    private final ApplicationConf userDetailsService;
    private final ObjectMapper mapper;

    @Value("${app.version}")
    private String appVersion;

    private static final String PUBLIC_SERVICE = "Auth";

    OperationReturnObject errorDetails = new OperationReturnObject();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        log.info("App version is {}", appVersion);

        CachedRequest wrappedRequest = new CachedRequest(request);

        String service = extractServiceFromRequest(wrappedRequest);
        log.info("Service requested: {}", service);

        if (PUBLIC_SERVICE.equalsIgnoreCase(service)) {
            log.info("Auth service accessed, skipping JWT validation");
            filterChain.doFilter(wrappedRequest, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userTag;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            errorDetails.setReturnCodeAndReturnMessage(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Authorization header is missing or invalid"
            );
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(String.valueOf(MediaType.APPLICATION_JSON));
            mapper.writeValue(response.getWriter(), errorDetails);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            userTag = jwtUtility.extractUsername(jwt);

            if (userTag != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                SystemUserModel userDetails = userDetailsService.loadUserByUsername(userTag);

                if (userDetails == null) {
                    throw new IllegalStateException("User not found");
                }

                if (jwtUtility.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        } catch (ExpiredJwtException e) {
            errorDetails.setReturnCodeAndReturnMessage(
                    HttpStatus.UNAUTHORIZED.value(),
                    "TOKEN EXPIRED"
            );
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(String.valueOf(MediaType.APPLICATION_JSON));
            mapper.writeValue(response.getWriter(), errorDetails);
            return;
        } catch (Exception e) {
            log.error("JWT validation error: ", e);
            errorDetails.setReturnCodeAndReturnMessage(
                    HttpStatus.UNAUTHORIZED.value(),
                    e.getMessage()
            );
            response.setStatus(HttpStatus.OK.value());
            response.setContentType(String.valueOf(MediaType.APPLICATION_JSON));
            mapper.writeValue(response.getWriter(), errorDetails);
            return;
        }

        filterChain.doFilter(wrappedRequest, response);
    }

    private String extractServiceFromRequest(CachedRequest request) {
        try {
            byte[] content = request.getCachedBody();

            if (content.length > 0) {
                String body = new String(content, StandardCharsets.UTF_8);
                log.debug("Request body: {}", body);

                JsonNode jsonNode = mapper.readTree(body);

                if (jsonNode.isArray() && jsonNode.size() >= 2) {
                    String service = jsonNode.get(1).asText();
                    log.info("Parsed Service: {}", service);
                    return service;
                }

                if (jsonNode.has("SERVICE")) {
                    return jsonNode.get("SERVICE").asText();
                } else if (jsonNode.has("service")) {
                    return jsonNode.get("service").asText();
                }
            }
        } catch (Exception e) {
            log.error("Error extracting service from request: ", e);
        }

        return null;

    }
}