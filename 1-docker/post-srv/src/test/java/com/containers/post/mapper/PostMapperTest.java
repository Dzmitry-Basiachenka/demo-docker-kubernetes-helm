package com.containers.post.mapper;

import com.containers.post.dto.PostDto;
import com.containers.post.entity.Post;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PostMapperTest {

    private final PostMapper postMapper = new PostMapperImpl();

    @Test
    public void testMapToDto_WhenPostPassed_ExpectDtoWithSameFields() {
        var post = new Post("post1", "user1", "hello", Instant.now());

        var postDto = postMapper.toDto(post);

        assertEquals(post.id(), postDto.id());
        assertEquals(post.userId(), postDto.userId());
        assertEquals(post.text(), postDto.text());
        assertEquals(post.postedAt(), postDto.postedAt());
    }

    @Test
    public void testMapToEntity_WhenPostDtoPassed_ExpectEntityWithSameFields() {
        var postDto = new PostDto("post1", "user1", "hello", Instant.now());

        var post = postMapper.toEntity(postDto);

        assertEquals(postDto.id(), post.id());
        assertEquals(postDto.userId(), post.userId());
        assertEquals(postDto.text(), post.text());
        assertEquals(postDto.postedAt(), post.postedAt());
    }
}
