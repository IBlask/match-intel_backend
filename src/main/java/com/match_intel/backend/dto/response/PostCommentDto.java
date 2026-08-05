package com.match_intel.backend.dto.response;

import com.match_intel.backend.entity.PostComment;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

public record PostCommentDto(
        UUID id,
        UserDto user,
        String comment,
        String createdAt
) {
    public static PostCommentDto fromEntity(PostComment postComment) {
        UserDto userDto = new UserDto();
        userDto.setId(postComment.getUser().getId());
        userDto.setFirstName(postComment.getUser().getFirstName());
        userDto.setLastName(postComment.getUser().getLastName());
        userDto.setUsername(postComment.getUser().getUsername());
        return new PostCommentDto(
                postComment.getId(),
                userDto,
                postComment.getComment(),
                postComment.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
}
