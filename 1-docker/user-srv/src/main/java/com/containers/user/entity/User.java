package com.containers.user.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "User")
public record User(

    @Id
    String id,

    String userName,

    int postsNumber
) {
}
