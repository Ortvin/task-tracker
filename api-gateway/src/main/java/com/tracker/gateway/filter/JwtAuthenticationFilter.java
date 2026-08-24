package com.tracker.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (path.startsWith("/auth/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/api-docs")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        log.info("Auth header: {}", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("No token or invalid format");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        log.info("Token: {}", token);

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            log.info("Claims: {}", claims);

            String role = claims.get("role", String.class);
            String username = claims.getSubject();
            String userId = claims.get("userId", String.class);

            log.info("Role from claims: {}", role);
            log.info("Adding headers: X-User-Id={}, X-Role={}", userId, role);

            ServerHttpRequest newRequest = exchange.getRequest().mutate()
                    .header("X-Username", username)
                    .header("X-Role", role)
                    .header("X-User-Id", userId)
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(newRequest)
                    .build();

            log.info("Headers after mutate: {}", mutatedExchange.getRequest().getHeaders());
            return chain.filter(mutatedExchange);

        } catch (Exception e) {
            log.error("Token validation error", e);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }
}