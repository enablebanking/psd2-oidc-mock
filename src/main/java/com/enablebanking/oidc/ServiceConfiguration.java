package com.enablebanking.oidc;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "service")
@Data
public class ServiceConfiguration {
    private String privateKeyPath;
    private String jwksPath;
    private String clientId;
    private String redirectUri;
    private String issuer;
    private Integer userId;
    private String salt;
}
