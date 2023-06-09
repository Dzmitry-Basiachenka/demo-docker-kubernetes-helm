package com.containers.user.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Null;

public record UserDto(

    @NotBlank(groups = CreateValidation.class)
    @Null(groups = UpdateValidation.class)
    String id,

    @NotBlank(groups = {CreateValidation.class, UpdateValidation.class})
    String userName,

    @Null(groups = {CreateValidation.class, UpdateValidation.class})
    Integer postsNumber
) {
}
