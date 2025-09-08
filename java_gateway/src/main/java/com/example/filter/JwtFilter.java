package com.example.filter;

import java.nio.charset.StandardCharsets;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.example.config.JwtConfig;
import com.example.util.JwtUtil;

import reactor.core.publisher.Mono;

@Component
public class JwtFilter extends AbstractGatewayFilterFactory<JwtFilter.Config> {
    // private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JwtConfig jwtConfig;

    public JwtFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // 确保依赖注入完成（防止NPE）
            if (jwtConfig == null || jwtUtil == null)
                return handleConfigError(exchange);

            String headerName = jwtConfig.getHeader();
            if (headerName == null)
                return handleConfigError(exchange);

            String authHeader = exchange.getRequest().getHeaders().getFirst(headerName);

            // 验证逻辑
            if (authHeader == null || !authHeader.startsWith("Bearer "))
                return writeErrorResponse(exchange, "Missing or invalid authorization header");

            String token = authHeader.substring(7);
            if (token.isEmpty())
                return writeErrorResponse(exchange, "Empty token");

            if (jwtUtil.validateToken(token))
                return chain.filter(exchange);
            else
                return writeErrorResponse(exchange, "Invalid token");

        };
    }

    private Mono<Void> handleConfigError(ServerWebExchange exchange) {
        return writeErrorResponse(exchange, "Server configuration error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, String message) {
        return writeErrorResponse(exchange, message, HttpStatus.UNAUTHORIZED);
    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String jsonBody = String.format("{\"code\":%d,\"message\":\"%s\"}",
                status.value(), message);

        DataBuffer buffer = response.bufferFactory().wrap(jsonBody.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
    }
}
