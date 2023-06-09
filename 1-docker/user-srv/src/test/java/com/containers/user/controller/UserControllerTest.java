package com.containers.user.controller;

import com.containers.user.dto.UserDto;
import com.containers.user.dto.UserUpdatePostsNumberDto;
import com.containers.user.entity.User;
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

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureDataMongo
@AutoConfigureWebTestClient
@WireMockTest(httpPort = 9000)
class UserControllerTest {

    private static final String PATH = "/api/v1/users";

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ReactiveMongoTemplate mongoTemplate;

    @Autowired
    private MockPostService mockPostService;

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection(User.class).block();
    }

    @Test
    void testFindAllUsers_WhenUsersDoNotExist_ExpectOkStatusAndEmptyList() {
        webTestClient.get()
            .uri(PATH)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBodyList(UserDto.class).isEqualTo(List.of());
    }

    @Test
    void testFindAllUsers_WhenUsersExist_ExpectOkStatusAndNotEmptyList() {
        var user = new User("user1", "John Doe", 0);
        var userDto = new UserDto(user.id(), user.userName(), user.postsNumber());

        mongoTemplate.save(user).block();

        webTestClient.get()
            .uri(PATH)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBodyList(UserDto.class).isEqualTo(List.of(userDto));
    }

    @Test
    void testFindUser_WhenUserDoesNotExist_ExpectNotFoundStatus() {
        webTestClient.get()
            .uri(PATH + "/{userId}", "user1")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testFindUser_WhenUserExists_ExpectOkStatus() {
        var user = new User("user1", "John Doe", 0);
        var userDto = new UserDto(user.id(), user.userName(), user.postsNumber());

        mongoTemplate.save(user).block();

        webTestClient.get()
            .uri(PATH + "/{userId}", user.id())
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBody(UserDto.class).isEqualTo(userDto);
    }

    @ParameterizedTest
    @MethodSource("invalidCreateUserMethodSource")
    void testCreateUser_WhenDataIsNotValid_ExpectBadRequestStatus(UserDto userDto) {
        webTestClient.post()
            .uri(PATH)
            .bodyValue(userDto)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);

        verifyUsersDoNotExist();
    }

    private static Stream<Arguments> invalidCreateUserMethodSource() {
        return Stream.of(
                new UserDto(null, "John Doe", null),
                new UserDto("", "John Doe", null),
                new UserDto(" ", "John Doe", null),
                new UserDto("user1", null, null),
                new UserDto("user1", "", null),
                new UserDto("user1", " ", null),
                new UserDto("user1", "John Doe", 0))
            .map(Arguments::of);
    }

    @Test
    void testCreateUser_WhenDataIsValid_ExpectOkStatusAndCreatedUser() {
        var userDto = new UserDto("user1", "John Doe", null);

        webTestClient.post()
            .uri(PATH)
            .bodyValue(userDto)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(UserDto.class)
            .value(dto -> verifyUserExists(dto.id(), userDto.userName()));
    }

    @Test
    void testUpdateUserName_WhenUserDoesNotExist_ExpectNotFoundStatus() {
        var userDto = new UserDto(null, "Richard Roe", null);

        webTestClient.put()
            .uri(PATH + "/{userId}", "user1")
            .bodyValue(userDto)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.NOT_FOUND);
    }

    @ParameterizedTest
    @MethodSource("invalidUpdateUserNameMethodSource")
    void testUpdateUserName_WhenDataIsNotValid_ExpectBadRequestStatus(UserDto userDto) {
        webTestClient.put()
            .uri(PATH + "/{userId}", "user1")
            .bodyValue(userDto)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private static Stream<Arguments> invalidUpdateUserNameMethodSource() {
        return Stream.of(
                new UserDto("1", "Richard Doe", null),
                new UserDto(null, null, null),
                new UserDto(null, "", null),
                new UserDto(null, " ", null),
                new UserDto(null, "Richard Doe", 0))
            .map(Arguments::of);
    }

    @Test
    void testUpdateUserName_WhenDataIsValidAndUserExists_ExpectOkStatusAndUpdatedUser() {
        var user = new User("user1", "John Doe", 0);
        var userDtoIn = new UserDto(null, "Richard Roe", null);
        var userDtoOut = new UserDto("user1", "Richard Roe", 0);

        mongoTemplate.save(user).block();

        webTestClient.put()
            .uri(PATH + "/{userId}", user.id())
            .bodyValue(userDtoIn)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBody(UserDto.class).isEqualTo(userDtoOut)
            .value(dto -> verifyUserExists(dto.id(), "Richard Roe", 0));
    }

    @Test
    void testUpdateUserPostsNumber_WhenUserDoesNotExist_ExpectNotFoundStatus() {
        var userUpdatePostsNumberDto = new UserUpdatePostsNumberDto(0);

        webTestClient.patch()
            .uri(PATH + "/{userId}", "user1")
            .bodyValue(userUpdatePostsNumberDto)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.NOT_FOUND);
    }

    @ParameterizedTest
    @MethodSource("invalidUpdateUserPostsNumberMethodSource")
    void testUpdateUserPostsNumber_WhenDataIsNotValid_ExpectBadRequestStatus(UserUpdatePostsNumberDto userUpdatePostsNumberDto) {
        webTestClient.patch()
            .uri(PATH + "/{userId}", "user1")
            .bodyValue(userUpdatePostsNumberDto)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private static Stream<Arguments> invalidUpdateUserPostsNumberMethodSource() {
        return Stream.of(
                new UserUpdatePostsNumberDto(null),
                new UserUpdatePostsNumberDto(-1))
            .map(Arguments::of);
    }

    @Test
    void testUpdateUserPostsNumber_WhenDataIsValidAndUserExists_ExpectOkStatusAndUpdatedUser() {
        var user = new User("user1", "John Doe", 0);
        var userUpdatePostsNumberDto = new UserUpdatePostsNumberDto(1);
        var userDto = new UserDto("user1", "John Doe", 1);

        mongoTemplate.save(user).block();

        webTestClient.patch()
            .uri(PATH + "/{userId}", user.id())
            .bodyValue(userUpdatePostsNumberDto)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.OK)
            .expectBody(UserDto.class).isEqualTo(userDto)
            .value(dto -> verifyUserExists(dto.id(), "John Doe", 1));
    }

    @Test
    void testDeleteUser_WhenUserDoesNotExist_ExpectNotFoundStatus() {
        String userId = "user1";

        mockPostService.deleteAllPostsByUserId(userId, HttpStatus.NO_CONTENT);

        webTestClient.delete()
            .uri(PATH + "/{userId}", userId)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void testDeleteUser_WhenUserExists_ExpectOkStatusAndUserDeleted() {
        var user = new User("user1", "John Doe", 0);

        mongoTemplate.save(user).block();

        mockPostService.deleteAllPostsByUserId(user.id(), HttpStatus.NO_CONTENT);

        webTestClient.delete()
            .uri(PATH + "/{userId}", user.id())
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.NO_CONTENT)
            .expectBody().isEmpty();

        verifyUserDoesNotExist(user.id());
    }

    private void verifyUsersDoNotExist() {
        StepVerifier.create(mongoTemplate.findAll(User.class))
            .expectNextCount(0)
            .verifyComplete();
    }

    private void verifyUserDoesNotExist(String userId) {
        StepVerifier.create(mongoTemplate.findById(userId, User.class))
            .expectNextCount(0)
            .verifyComplete();
    }

    private void verifyUserExists(String userId, String userName) {
        verifyUserExists(userId, user -> userName.equals(user.userName()));
    }

    private void verifyUserExists(String userId, String userName, Integer postsNumber) {
        verifyUserExists(userId, user -> userName.equals(user.userName()) && postsNumber.equals(user.postsNumber()));
    }

    private void verifyUserExists(String userId, Predicate<User> matcher) {
        StepVerifier.create(mongoTemplate.findById(userId, User.class))
            .expectNextMatches(matcher)
            .verifyComplete();
    }
}
