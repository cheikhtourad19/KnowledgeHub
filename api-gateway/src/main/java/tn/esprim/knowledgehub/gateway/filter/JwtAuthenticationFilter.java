package tn.esprim.knowledgehub.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import javax.crypto.SecretKey;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Value("${jwt.secret}")
    private String secret;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (isPublicEndpoint(request.getPath().toString())) {
                return chain.filter(exchange);
            }

            if (!request.getHeaders().containsKey("Authorization")) {
                System.err.println("Missing Authorization header");
                return onError(exchange, "Missing authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.err.println("Invalid auth header format: " + authHeader);
                return onError(exchange, "Invalid authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);
            System.out.println("Token: " + token.substring(0, Math.min(20, token.length())) + "...");

            try {
                Claims claims = validateToken(token);
                System.out.println("Claims: " + claims);

                String authId = claims.get("authId", String.class);
                String role = claims.get("role", String.class);

                if (authId == null || role == null) {
                    System.err.println("Missing required claims. authId: " + authId + ", role: " + role);
                    return onError(exchange, "Invalid token claims", HttpStatus.UNAUTHORIZED);
                }

                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-Auth-Id", authId)
                        .header("X-User-Name", claims.getSubject())
                        .header("X-User-Role", role)
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } catch (Exception e) {
                System.err.println("Token validation failed: " + e.getMessage());
                e.printStackTrace();
                return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Claims validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isPublicEndpoint(String path) {

        if (path.matches("(.*/)?public(/.*)?")) {
            return true;
        }


        return false;
    }

    private reactor.core.publisher.Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // Configuration properties if needed
    }
}