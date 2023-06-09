package com.containers.user.api;

import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface PostServiceClient {

    Mono<ResponseEntity<Object>> deleteAllPostsByUserId(String userId);
}
