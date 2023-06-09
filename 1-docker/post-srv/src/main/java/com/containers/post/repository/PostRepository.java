package com.containers.post.repository;

import com.containers.post.entity.Post;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PostRepository extends ReactiveMongoRepository<Post, String> {

    Flux<Post> findAllByUserId(String userId);

    Mono<Long> countByUserId(String userId);

    Mono<Void> deleteAllByUserId(String userId);
}
