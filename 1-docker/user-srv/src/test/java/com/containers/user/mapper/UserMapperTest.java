package com.containers.user.mapper;

import com.containers.user.dto.UserDto;
import com.containers.user.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserMapperTest {

    private final UserMapper userMapper = new UserMapperImpl();

    @Test
    public void testMapToDto_WhenUserPassed_ExpectDtoWithSameFields() {
        var user = new User("user1", "John Doe", 0);

        var userDto = userMapper.toDto(user);

        assertEquals(user.id(), userDto.id());
        assertEquals(user.userName(), userDto.userName());
        assertEquals(user.postsNumber(), userDto.postsNumber());
    }

    @Test
    public void testMapToEntity_WhenUserDtoPassed_ExpectEntityWithSameFields() {
        var userDto = new UserDto("user1", "John Doe", 0);

        var user = userMapper.toEntity(userDto);

        assertEquals(userDto.id(), user.id());
        assertEquals(userDto.userName(), user.userName());
        assertEquals(userDto.postsNumber(), user.postsNumber());
    }
}
