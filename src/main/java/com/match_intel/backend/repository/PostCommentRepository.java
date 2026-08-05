package com.match_intel.backend.repository;

import com.match_intel.backend.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PostCommentRepository extends JpaRepository<PostComment, UUID> {
    List<PostComment> findAllByPost_Id(UUID postId);
}
