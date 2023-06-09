package com.containers.user.mapper;

import com.containers.user.dto.UserDto;
import com.containers.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserDto userDto);

    UserDto toDto(User user);
}
