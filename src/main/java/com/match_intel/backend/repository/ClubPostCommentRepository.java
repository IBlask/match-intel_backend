package com.match_intel.backend.repository;

import com.match_intel.backend.entity.ClubPostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClubPostCommentRepository extends JpaRepository<ClubPostComment, UUID> {
    List<ClubPostComment> findAllByPost_Id(UUID postId);
    long countByPost_Id(UUID postId);
}
