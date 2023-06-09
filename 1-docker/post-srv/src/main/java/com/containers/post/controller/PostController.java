package com.containers.post.controller;

import com.containers.post.dto.CreateValidation;
import com.containers.post.dto.PostDto;
import com.containers.post.dto.UpdateValidation;
import com.containers.post.mapper.PostMapper;
import com.containers.post.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;
    private final PostMapper postMapper;

    public PostController(PostService postService, PostMapper postMapper) {
        this.postService = postService;
        this.postMapper = postMapper;
    }

    @GetMapping
    public Flux<PostDto> getAllPosts() {
        return postService.getAllPosts()
            .map(postMapper::toDto);
    }

    @GetMapping("/user/{userId}")
    public Flux<PostDto> findAllPostsByUserId(@PathVariable("userId") String userId) {
        return postService.findAllPostsByUserId(userId)
            .map(postMapper::toDto);
    }

    @GetMapping("{postId}")
    public Mono<PostDto> getPost(@PathVariable("postId") String postId) {
        return postService.getPost(postId)
            .map(postMapper::toDto);
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public Mono<PostDto> createPost(@Validated(CreateValidation.class) @RequestBody PostDto postDto) {
        var post = postMapper.toEntity(postDto);
        return postService.createPost(post)
            .map(postMapper::toDto);
    }

    @PutMapping("{postId}")
    @ResponseStatus(code = HttpStatus.OK)
    public Mono<PostDto> updatePostText(@PathVariable("postId") String postId, @Validated(UpdateValidation.class) @RequestBody PostDto postDto) {
        return postService.updatePostText(postId, postDto.text())
            .map(postMapper::toDto);
    }

    @DeleteMapping("/user/{userId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<Void> deleteAllPostsByUserId(@PathVariable("userId") String userId) {
        return postService.deleteAllPostsByUserId(userId);
    }

    @DeleteMapping("{postId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<Void> deletePost(@PathVariable("postId") String postId) {
        return postService.deletePost(postId);
    }
}
