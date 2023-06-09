package com.containers.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

@Component
public class MockPostService {

    public void deleteAllPostsByUserId(String userId, HttpStatus status) {
        var builder = aResponse()
            .withStatus(status.value());
        stubFor(delete(urlPathEqualTo("/api/v1/posts/user/" + userId))
            .willReturn(builder));
    }
}
