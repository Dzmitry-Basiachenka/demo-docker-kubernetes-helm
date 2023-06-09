package com.containers.post.controller;

import com.containers.post.dto.PostDto;
import com.containers.post.dto.UserUpdatePostsNumberDto;
import com.containers.post.entity.Post;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureDataMongo
@AutoConfigureWebTestClient
@WireMockTest(httpPort = 9000)
class PostControllerTest {

    private static final String PATH = "/api/v1/posts";

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ReactiveMongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection(Post.class).block();
    }

    @Test
    void testFindAllPosts_WhenPostsDoNotExist_ExpectOkStatusAndEmptyList() {
        webTestClient.get()
            .uri(PATH)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBodyList(PostDto.class).isEqualTo(List.of());
    }

    @Test
    void testFindAllPosts_WhenPostsExist_ExpectOkStatusAndNotEmptyList() {
        var post = new Post("post1", "user1", "one", Instant.EPOCH);
        var postDto = new PostDto(post.id(), post.userId(), post.text(), post.postedAt());

        mongoTemplate.save(post).block();

        webTestClient.get()
            .uri(PATH)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBodyList(PostDto.class).isEqualTo(List.of(postDto));
    }

    @Test
    void testFindAllPostsByUserId_WhenPostsDoNotExist_ExpectOkStatusAndEmptyList() {
        webTestClient.get()
            .uri(PATH + "/user/{userId}", "user1")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBodyList(PostDto.class).isEqualTo(List.of());
    }

    @Test
    void testFindAllPostsId_WhenPostsExist_ExpectOkStatusAndNotEmptyList() {
        var post = new Post("post1", "user1", "one", Instant.EPOCH);
        var postDto = new PostDto(post.id(), post.userId(), post.text(), post.postedAt());

        mongoTemplate.save(post).block();

        webTestClient.get()
            .uri(PATH + "/user/{userId}", "user1")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBodyList(PostDto.class).isEqualTo(List.of(postDto));
    }

    @Test
    void testFindAllPostsId_WhenPostsDoNotExist_ExpectOkStatusAndEmptyList() {
        var post = new Post("post1", "user1", "one", Instant.EPOCH);

        mongoTemplate.save(post).block();

        webTestClient.get()
            .uri(PATH + "/user/{userId}", "user2")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBodyList(PostDto.class).isEqualTo(List.of());
    }

    @Test
    void testFindPost_WhenPostDoesNotExist_ExpectNotFoundStatus() {
        webTestClient.get()
            .uri(PATH + "/{postId}", "post1")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testFindPost_WhenPostExists_ExpectOkStatus() {
        var post = new Post("post1", "user1", "one", Instant.EPOCH);
        var postDto = new PostDto(post.id(), post.userId(), post.text(), post.postedAt());

        mongoTemplate.save(post).block();

        webTestClient.get()
            .uri(PATH + "/{postId}", post.id())
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBody(PostDto.class).isEqualTo(postDto);
    }

    @ParameterizedTest
    @MethodSource("invalidCreatePostMethodSource")
    void testCreatePost_WhenDataIsNotValid_ExpectBadRequestStatus(PostDto postDto) {
        webTestClient.post()
            .uri(PATH)
            .bodyValue(postDto)
            .exchange()
            .expectStatus().isBadRequest();

        verifyPostsDoNotExist();
    }

    private static Stream<Arguments> invalidCreatePostMethodSource() {
        var postId = "post1";
        var userId = "user1";
        var text = "one";
        var postedAt = Instant.EPOCH;
        return Stream.of(
                new PostDto(null, userId, text, null),
                new PostDto("", userId, text, null),
                new PostDto(" ", userId, text, null),
                new PostDto(postId, null, text, null),
                new PostDto(postId, "", text, null),
                new PostDto(postId, " ", text, null),
                new PostDto(postId, userId, null, null),
                new PostDto(postId, userId, "", null),
                new PostDto(postId, userId, " ", null),
                new PostDto(postId, userId, text, postedAt))
            .map(Arguments::of);
    }

    @Test
    void testCreatePost_WhenUserNotFound_ExpectConflictStatus() {
        var userId = "user1";
        var postDto = new PostDto("post1", userId, "one", null);

        MockUserService.mockUserUpdatePostsNumber(userId, HttpStatus.NOT_FOUND);

        webTestClient.post()
            .uri(PATH)
            .bodyValue(postDto)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CONFLICT);

        verifyPostsDoNotExist();
    }

    @Test
    void testCreatePost_WhenDataIsValid_ExpectOkStatusAndCreatedPost() {
        var userId = "user1";
        var postDto = new PostDto("post1", userId, "one", null);

        MockUserService.mockUserUpdatePostsNumber(userId, new UserUpdatePostsNumberDto(1));

        webTestClient.post()
            .uri(PATH)
            .bodyValue(postDto)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CREATED)
            .expectBody(PostDto.class)
            .value(dto -> verifyPostExists(dto, postDto.userId(), postDto.text()));
    }

    @Test
    void testUpdatePost_WhenPostDoesNotExist_ExpectNotFoundStatus() {
        var postDto = new PostDto(null, null, "one", null);

        webTestClient.put()
            .uri(PATH + "/{postId}", "post1")
            .bodyValue(postDto)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.NOT_FOUND);
    }

    @ParameterizedTest
    @MethodSource("invalidUpdatePostMethodSource")
    void testUpdatePost_WhenDataIsNotValid_ExpectBadRequestStatus(PostDto postDto) {
        webTestClient.put()
            .uri(PATH + "/{postId}", "post1")
            .bodyValue(postDto)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private static Stream<Arguments> invalidUpdatePostMethodSource() {
        var postId = "post1";
        var userId = "user1";
        var text = "one";
        var postedAt = Instant.EPOCH;
        return Stream.of(
                new PostDto(postId, null, text, null),
                new PostDto(null, userId, text, null),
                new PostDto(null, null, null, null),
                new PostDto(null, null, "", null),
                new PostDto(null, null, " ", null),
                new PostDto(null, null, text, postedAt))
            .map(Arguments::of);
    }

    @Test
    void testUpdatePost_WhenDataIsValidAndPostExists_ExpectOkStatusAndUpdatedPost() {
        var post = new Post("post1", "user1", "one", null);
        var postDtoIn = new PostDto(null, null, "two", null);

        mongoTemplate.save(post).block();

        webTestClient.put()
            .uri(PATH + "/{postId}", post.id())
            .bodyValue(postDtoIn)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBody(PostDto.class).value(
                postDto -> assertTrue(
                    "post1".equals(postDto.id())
                        && "user1".equals(postDto.userId())
                        && "two".equals(postDto.text())
                        && postDto.postedAt() != null
                )
            )
            .value(dto -> verifyPostExists(dto, "user1", "two"));
    }

    @Test
    void testDeleteAllPostsId_WhenPostsDoNotExist_ExpectOkStatusAndNotEmptyList() {
        webTestClient.delete()
            .uri(PATH + "/user/{userId}", "user1")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.NO_CONTENT)
            .expectBody().isEmpty();

        verifyPostsDoNotExist();
    }

    @Test
    void testDeleteAllPostsId_WhenPostsExist_ExpectOkStatusAndNotEmptyList() {
        var post1 = new Post("post1", "user1", "one", Instant.EPOCH);
        var post2 = new Post("post2", "user2", "one", Instant.EPOCH);

        mongoTemplate.save(post1).block();
        mongoTemplate.save(post2).block();

        webTestClient.delete()
            .uri(PATH + "/user/{userId}", "user1")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.NO_CONTENT)
            .expectBody().isEmpty();

        verifyPostDoesNotExist(post1.id());
        verifyPostExists(post2.id());
    }

    @Test
    void testDeletePost_WhenPostDoesNotExist_ExpectNotFoundStatus() {
        webTestClient.delete()
            .uri(PATH + "/{postId}", 1)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testDeletePost_WhenPostExists_ExpectOkStatusAndPostDeleted() {
        var post = new Post("post1", "user1", "one", Instant.EPOCH);

        mongoTemplate.save(post).block();

        MockUserService.mockUserUpdatePostsNumber(post.userId(), new UserUpdatePostsNumberDto(0));

        webTestClient.delete()
            .uri(PATH + "/{postId}", post.id())
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.NO_CONTENT)
            .expectBody().isEmpty();

        verifyPostDoesNotExist(post.id());
    }

    @Test
    void testDeletePost_WhenUserNotFound_ExpectOkStatusAndPostDeleted() {
        var post = new Post("post1", "user1", "one", Instant.EPOCH);

        mongoTemplate.save(post).block();

        MockUserService.mockUserUpdatePostsNumber(post.userId(), HttpStatus.NOT_FOUND);

        webTestClient.delete()
            .uri(PATH + "/{postId}", post.id())
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.NO_CONTENT)
            .expectBody().isEmpty();

        verifyPostDoesNotExist(post.id());
    }

    private void verifyPostsDoNotExist() {
        StepVerifier.create(mongoTemplate.findAll(Post.class))
            .expectNextCount(0)
            .verifyComplete();
    }

    private void verifyPostDoesNotExist(String postId) {
        StepVerifier.create(mongoTemplate.findById(postId, Post.class))
            .expectNextCount(0)
            .verifyComplete();
    }

    private void verifyPostExists(String postId) {
        StepVerifier.create(mongoTemplate.findById(postId, Post.class))
            .expectNextCount(1)
            .verifyComplete();
    }

    private void verifyPostExists(PostDto postDto, String userId, String text) {
        verifyPostExists(postDto, post -> userId.equals(post.userId()) && text.equals(post.text()));
    }

    private void verifyPostExists(PostDto postDto, Predicate<Post> predicate) {
        StepVerifier.create(mongoTemplate.findById(postDto.id(), Post.class))
            .expectNextMatches(predicate)
            .verifyComplete();
    }
}
