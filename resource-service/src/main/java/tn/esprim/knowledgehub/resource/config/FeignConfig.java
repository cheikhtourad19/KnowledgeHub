package tn.esprim.knowledgehub.resource.config;

import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
                10000,  // 10 seconds connect timeout
                600000  // 10 minutes read timeout for AI
        );
    }
}