package com.containers.user.api;

import com.containers.user.properties.PostApiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Component
public class PostServiceWebClient implements PostServiceClient {

    private final WebClient webClient;

    @Autowired
    public PostServiceWebClient(PostApiProperties properties) {
        this.webClient = WebClient.builder()
            .baseUrl(properties.url() + "/api/v1/posts/")
            .build();
    }

    @Override
    public Mono<ResponseEntity<Object>> deleteAllPostsByUserId(String userId) {
        return webClient.delete()
            .uri("user/{userId}", userId)
            .retrieve()
            .toEntity(Object.class)
            .onErrorResume(WebClientResponseException.class, processResponseStatus());
    }

    private Function<WebClientResponseException, Mono<? extends ResponseEntity<Object>>> processResponseStatus() {
        return e -> Mono.error(e);
    }
}
