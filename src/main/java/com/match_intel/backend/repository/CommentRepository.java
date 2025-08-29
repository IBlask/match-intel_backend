package com.match_intel.backend.repository;

import com.match_intel.backend.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findAllByMatch_Id(UUID matchId);
}
