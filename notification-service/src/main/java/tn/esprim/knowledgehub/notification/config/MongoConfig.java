package tn.esprim.knowledgehub.notification.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "tn.esprim.knowledgehub.notification.repository")
@EnableMongoAuditing
public class MongoConfig {
}