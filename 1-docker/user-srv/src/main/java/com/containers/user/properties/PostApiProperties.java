package com.containers.user.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "api.post-api")
public record PostApiProperties(

    String url
) {
}
