package com.containers.user.controller;

import com.containers.user.dto.CreateValidation;
import com.containers.user.dto.UpdateValidation;
import com.containers.user.dto.UserDto;
import com.containers.user.dto.UserUpdatePostsNumberDto;
import com.containers.user.mapper.UserMapper;
import com.containers.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping
    public Flux<UserDto> getAllUsers() {
        return userService.getAllUsers()
            .map(userMapper::toDto);
    }

    @GetMapping("{userId}")
    public Mono<UserDto> getUser(@PathVariable("userId") String userId) {
        return userService.getUser(userId)
            .map(userMapper::toDto);
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public Mono<UserDto> createUser(@Validated(CreateValidation.class) @RequestBody UserDto userDto) {
        var user = userMapper.toEntity(userDto);
        return userService.createUser(user)
            .map(userMapper::toDto);
    }

    @PutMapping("{userId}")
    @ResponseStatus(code = HttpStatus.OK)
    public Mono<UserDto> updateUserName(@PathVariable("userId") String userId, @Validated(UpdateValidation.class) @RequestBody UserDto userDto) {
        return userService.updateUserName(userId, userDto.userName())
            .map(userMapper::toDto);
    }

    @PatchMapping("{userId}")
    @ResponseStatus(code = HttpStatus.OK)
    public Mono<UserDto> updateUserPostsNumber(@PathVariable("userId") String userId, @Valid @RequestBody UserUpdatePostsNumberDto userUpdatePostsNumberDto) {
        return userService.updatePostsNumber(userId, userUpdatePostsNumberDto.postsNumber())
            .map(userMapper::toDto);
    }

    @DeleteMapping("{userId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<Void> deleteUser(@PathVariable("userId") String userId) {
        return userService.deleteUser(userId);
    }
}
