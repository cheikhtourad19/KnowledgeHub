package tn.esprim.knowledgehub.user;


import com.knowledgehub.notification.client.config.EnableNotificationClient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableNotificationClient
public class UserServiceApplication {
    public static void main(String[] args) {

        SpringApplication.run(UserServiceApplication.class, args);
    }
}
