package com.containers.post.api;

import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface UserServiceClient {

    Mono<ResponseEntity<Object>> updateUserPostsNumber(String userId, int postsNumber);
}
