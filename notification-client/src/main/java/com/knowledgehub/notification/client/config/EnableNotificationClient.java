package com.knowledgehub.notification.client.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({NotificationClientConfig.class, NotificationClientAsyncConfig.class})
@ComponentScan(basePackages = "com.knowledgehub.notification.client")
public @interface EnableNotificationClient {
}
