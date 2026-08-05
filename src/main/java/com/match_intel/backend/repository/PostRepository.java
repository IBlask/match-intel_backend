package com.match_intel.backend.repository;

import com.match_intel.backend.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {

    List<Post> findByAuthor_UsernameOrderByCreatedAtDesc(String username);

    @Query("""
        SELECT p FROM Post p
        JOIN FollowRequest fr ON p.author.id = fr.followee.id
        WHERE fr.follower.id = :followerId
          AND fr.status = 'ACCEPTED'
          AND p.visibility IN ('PUBLIC', 'FOLLOWERS')
        ORDER BY p.createdAt DESC
    """)
    List<Post> findVisiblePostsOfFollowees(@Param("followerId") UUID followerId);
}
