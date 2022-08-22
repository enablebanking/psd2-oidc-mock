package com.enablebanking.oidc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ServiceConfiguration.class)
public class OIDCMockApplication {

    public static void main(String[] args) {
        SpringApplication.run(OIDCMockApplication.class, args);
    }

}
