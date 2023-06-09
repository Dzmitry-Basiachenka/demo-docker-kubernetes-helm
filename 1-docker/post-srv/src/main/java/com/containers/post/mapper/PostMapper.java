package com.containers.post.mapper;

import com.containers.post.dto.PostDto;
import com.containers.post.entity.Post;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PostMapper {

    Post toEntity(PostDto postDto);

    PostDto toDto(Post post);
}
