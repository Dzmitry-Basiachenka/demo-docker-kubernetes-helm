package com.containers.post.api;

import com.containers.post.dto.UserUpdatePostsNumberDto;
import com.containers.post.properties.UserApiProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Component
public class UserServiceWebClient implements UserServiceClient {

    private final WebClient webClient;

    @Autowired
    public UserServiceWebClient(UserApiProperties properties) {
        this.webClient = WebClient.builder()
            .baseUrl(properties.url() + "/api/v1/users/")
            .build();
    }

    @Override
    public Mono<ResponseEntity<Object>> updateUserPostsNumber(String userId, int postsNumber) {
        return webClient.patch()
            .uri("{userId}", userId)
            .body(Mono.just(new UserUpdatePostsNumberDto(postsNumber)), UserUpdatePostsNumberDto.class)
            .retrieve()
            .toEntity(Object.class)
            .onErrorResume(WebClientResponseException.class, processResponseStatus());
    }

    private Function<WebClientResponseException, Mono<? extends ResponseEntity<Object>>> processResponseStatus() {
        return e -> HttpStatus.NOT_FOUND.equals(e.getStatusCode())
            ? Mono.just(ResponseEntity.notFound().build())
            : Mono.error(e);
    }
}
