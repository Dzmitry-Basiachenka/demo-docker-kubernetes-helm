package com.containers.post.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "Post")
public record Post(

    @Id
    String id,

    String userId,

    String text,

    Instant postedAt
) {
}
