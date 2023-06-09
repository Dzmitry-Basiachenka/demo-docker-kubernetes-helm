package com.containers.post.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;
import java.time.Instant;

public record PostDto(

    @NotBlank(groups = CreateValidation.class)
    @Null(groups = UpdateValidation.class)
    String id,

    @NotBlank(groups = CreateValidation.class)
    @Null(groups = UpdateValidation.class)
    String userId,

    @NotBlank(groups = {CreateValidation.class, UpdateValidation.class})
    String text,

    @Null(groups = {CreateValidation.class, UpdateValidation.class})
    Instant postedAt
) {
}
