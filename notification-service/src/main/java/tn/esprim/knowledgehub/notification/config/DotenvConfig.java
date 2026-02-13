package tn.esprim.knowledgehub.notification.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Configuration to load environment variables from .env files
 * Priority: .env.local (local development) > .env (Docker/default)
 */
@Configuration
@PropertySource(value = "file:.env", ignoreResourceNotFound = true)
@PropertySource(value = "file:.env.local", ignoreResourceNotFound = true)
public class DotenvConfig {
    // Spring-dotenv library automatically loads .env files
    // This configuration ensures .env.local overrides .env when present
}
