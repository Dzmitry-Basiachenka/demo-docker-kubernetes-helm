package com.containers.post.controller;

import com.containers.post.dto.UserUpdatePostsNumberDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

public class MockUserService {

    public static void mockUserUpdatePostsNumber(String userId, UserUpdatePostsNumberDto userUpdatePostsNumberDto) {
        var builder = aResponse()
            .withStatus(HttpStatus.OK.value())
            .withHeader("Content-Type", "application/json")
            .withBody(toJson(userUpdatePostsNumberDto));
        stubFor(patch(urlPathEqualTo("/api/v1/users/" + userId))
            .willReturn(builder));
    }

    private static String toJson(UserUpdatePostsNumberDto userUpdatePostsNumberDto) {
        try {
            return new ObjectMapper().writeValueAsString(userUpdatePostsNumberDto);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void mockUserUpdatePostsNumber(String userId, HttpStatus status) {
        var builder = aResponse()
            .withStatus(status.value())
            .withHeader("Content-Type", "application/json");
        stubFor(patch(urlPathEqualTo("/api/v1/users/" + userId))
            .willReturn(builder));
    }
}
