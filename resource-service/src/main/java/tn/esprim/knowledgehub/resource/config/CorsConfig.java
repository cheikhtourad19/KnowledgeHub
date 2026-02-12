package tn.esprim.knowledgehub.resource.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:http://localhost:*,http://127.0.0.1:*}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Allow credentials (cookies, authorization headers)
        config.setAllowCredentials(true);

        // Allow origins from configuration
        // Default includes localhost for development
        // In production, set via application.yml or environment variables
        List<String> originPatterns = Arrays.asList(
                "http://localhost:*",           // Local development
                "http://127.0.0.1:*",           // Local development
                "http://localhost:8080",        // API Gateway (local)
                "http://localhost:3000",        // Common React dev port
                "http://localhost:4200",        // Common Angular dev port
                "http://localhost:5173",        // Vite dev port
                "https://localhost:*",          // HTTPS local
                "https://*.yourdomain.com",     // Production frontend domain
                "http://*.yourdomain.com",      // Production frontend domain (if HTTP)
                "https://yourdomain.com"        // Production frontend (main domain)
                // Add your production API Gateway URL here
                // "https://api.yourdomain.com"
        );

        config.setAllowedOriginPatterns(originPatterns);

        // Allow all HTTP methods
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Allow all headers
        config.addAllowedHeader("*");

        // Expose headers that the frontend might need
        config.setExposedHeaders(Arrays.asList(
                "Content-Disposition",
                "Content-Type",
                "Content-Length",
                "Authorization",
                "X-Auth-Id",
                "X-User-Role",
                "X-User-Email",
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"
        ));

        // Cache preflight requests for 1 hour
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
