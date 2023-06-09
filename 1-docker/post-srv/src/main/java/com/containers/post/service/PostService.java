package com.containers.post.service;

import com.containers.post.entity.Post;
import com.containers.post.exception.PostNotFoundException;
import com.containers.post.exception.UserNotFoundException;
import com.containers.post.repository.PostRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserApiService userApiService;

    public PostService(PostRepository postRepository, UserApiService userApiService) {
        this.postRepository = postRepository;
        this.userApiService = userApiService;
    }

    public Flux<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Flux<Post> findAllPostsByUserId(String userId) {
        return postRepository.findAllByUserId(userId);
    }

    public Mono<Post> getPost(String postId) {
        return postRepository.findById(postId)
            .switchIfEmpty(Mono.error(new PostNotFoundException(postId)));
    }

    public Mono<Post> createPost(Post post) {
        return userApiService.updateUserPostsNumber(post.userId(), 1)
            .flatMap(userExists -> userExists ? Mono.just(true) : Mono.error(new UserNotFoundException(post.userId())))
            .then(savePost(post));
    }

    public Mono<Post> updatePostText(String postId, String text) {
        return getPost(postId)
            .map(post -> new Post(post.id(), post.userId(), text, post.postedAt()))
            .flatMap(this::savePost);
    }

    private Mono<Post> savePost(Post post) {
        return postRepository.save(new Post(post.id(), post.userId(), post.text(), Instant.now()));
    }

    public Mono<Void> deleteAllPostsByUserId(String userId) {
        return postRepository.deleteAllByUserId(userId);
    }

    public Mono<Void> deletePost(String postId) {
        return getPost(postId)
            .flatMap(post -> postRepository.deleteById(post.id())
                .then(userApiService.updateUserPostsNumber(post.userId(), 0))
                .then());
    }
}
