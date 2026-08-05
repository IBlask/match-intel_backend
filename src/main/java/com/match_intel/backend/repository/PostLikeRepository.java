package com.match_intel.backend.repository;

import com.match_intel.backend.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostLikeRepository extends JpaRepository<PostLike, UUID> {
    Optional<PostLike> findByPost_IdAndUser_Username(UUID postId, String username);
    List<PostLike> findAllByPost_Id(UUID postId);
    long countByPost_Id(UUID postId);
}
