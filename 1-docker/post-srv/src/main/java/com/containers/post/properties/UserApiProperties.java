package com.containers.post.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "api.user-api")
public record UserApiProperties(

    String url
) {
}
