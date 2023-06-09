package com.containers.user.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public record UserUpdatePostsNumberDto(

    @NotNull
    @Min(0)
    Integer postsNumber
) {
}