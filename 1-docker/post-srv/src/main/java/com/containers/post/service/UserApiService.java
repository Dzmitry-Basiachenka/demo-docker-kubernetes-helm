package com.containers.post.service;

import com.containers.post.api.UserServiceClient;
import com.containers.post.repository.PostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class UserApiService {

    private final PostRepository postRepository;
    private final UserServiceClient userServiceClient;

    public UserApiService(PostRepository postRepository, UserServiceClient userServiceClient) {
        this.postRepository = postRepository;
        this.userServiceClient = userServiceClient;
    }

    public Mono<Boolean> updateUserPostsNumber(String userId, int postsChange) {
        return postRepository.countByUserId(userId)
            .flatMap(postsNumber -> userServiceClient.updateUserPostsNumber(userId, (int) (postsNumber + postsChange)))
            .map(ResponseEntity::getStatusCode)
            .flatMap(this::isUserExists);
    }

    private Mono<Boolean> isUserExists(HttpStatus status) {
        return switch (status) {
            case OK -> Mono.just(true);
            case NOT_FOUND -> Mono.just(false);
            default -> Mono.error(new RuntimeException("Wrong user service response status: " + status));
        };
    }
}
