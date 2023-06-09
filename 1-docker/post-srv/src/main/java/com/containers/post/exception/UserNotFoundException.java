package com.containers.post.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT)
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String userId) {
        super("User is not found by userId=" + userId);
    }
}
