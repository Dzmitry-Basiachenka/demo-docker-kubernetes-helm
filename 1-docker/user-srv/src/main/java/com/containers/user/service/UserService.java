package com.containers.user.service;

import com.containers.user.entity.User;
import com.containers.user.exception.UserNotFoundException;
import com.containers.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PostApiService postApiService;

    public UserService(UserRepository userRepository, PostApiService postApiService) {
        this.userRepository = userRepository;
        this.postApiService = postApiService;
    }

    public Flux<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Mono<User> getUser(String userId) {
        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(new UserNotFoundException(userId)));
    }

    public Mono<User> createUser(User user) {
        return userRepository.save(user);
    }

    public Mono<User> updateUserName(String userId, String userName) {
        return getUser(userId)
            .flatMap(user -> userRepository.save(new User(user.id(), userName, user.postsNumber())));
    }

    public Mono<User> updatePostsNumber(String userId, int postsNumber) {
        return getUser(userId)
            .flatMap(user -> userRepository.save(new User(user.id(), user.userName(), postsNumber)));
    }

    public Mono<Void> deleteUser(String userId) {
        return userRepository.deleteById(userId)
            .then(postApiService.deleteAllPostsByUserId(userId));
    }
}
