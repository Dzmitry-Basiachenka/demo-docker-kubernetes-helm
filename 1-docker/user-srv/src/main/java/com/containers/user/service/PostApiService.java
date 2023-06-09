package com.containers.user.service;

import com.containers.user.api.PostServiceClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class PostApiService {

    private final PostServiceClient userServiceClient;

    public PostApiService(PostServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    public Mono<Void> deleteAllPostsByUserId(String userId) {
        return userServiceClient.deleteAllPostsByUserId(userId)
            .then();
    }
}
