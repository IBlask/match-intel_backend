package com.match_intel.backend.service;

import com.match_intel.backend.dto.response.PostDto;
import com.match_intel.backend.dto.response.UserDto;
import com.match_intel.backend.entity.*;
import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.repository.FollowRequestRepository;
import com.match_intel.backend.repository.PostCommentRepository;
import com.match_intel.backend.repository.PostLikeRepository;
import com.match_intel.backend.repository.PostRepository;
import com.match_intel.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private PostLikeRepository postLikeRepository;
    @Autowired
    private PostCommentRepository postCommentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FollowRequestRepository followRequestRepository;


    public PostDto createPost(String username, String content, String imageUrl, MatchVisibility visibility) {
        if (content == null || content.isBlank()) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Post content cannot be empty");
        }

        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "User not found"));

        Post post = new Post();
        post.setAuthor(author);
        post.setContent(content);
        post.setImageUrl(imageUrl);
        post.setVisibility(visibility);
        postRepository.save(post);

        return toDto(post, false);
    }

    public List<PostDto> getVisiblePosts(String requesterUsername, String targetUsername) {
        User target = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Target user not found"));
        User requester = userRepository.findByUsername(requesterUsername)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Requester not found"));

        boolean restricted = false;
        if (!requesterUsername.equals(targetUsername)) {
            switch (target.getProfileVisibility()) {
                case PUBLIC -> { }
                case FOLLOWERS -> restricted = !isFollowing(requesterUsername, targetUsername);
                case PRIVATE -> restricted = true;
            }
        }

        if (restricted) {
            return List.of();
        }

        List<Post> posts = postRepository.findByAuthor_UsernameOrderByCreatedAtDesc(targetUsername);

        return posts.stream()
                .filter(post -> {
                    if (requesterUsername.equals(targetUsername)) {
                        return true;
                    }
                    return switch (post.getVisibility()) {
                        case PUBLIC -> true;
                        case FOLLOWERS -> isFollowing(requesterUsername, targetUsername);
                        case PRIVATE -> false;
                    };
                })
                .map(post -> toDto(post, isLikedByUser(post.getId(), requesterUsername)))
                .toList();
    }

    public List<PostDto> getFollowedPosts(String requesterUsername) {
        User requester = userRepository.findByUsername(requesterUsername)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Requester not found"));

        return postRepository.findVisiblePostsOfFollowees(requester.getId()).stream()
                .map(post -> toDto(post, isLikedByUser(post.getId(), requesterUsername)))
                .toList();
    }

    public PostDto getPost(String username, UUID postId) {
        Post post = getPostEntity(postId);
        return toDto(post, isLikedByUser(postId, username));
    }

    public void deletePost(String username, UUID postId) {
        Post post = getPostEntity(postId);
        if (!post.getAuthor().getUsername().equals(username)) {
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "Only the author can delete this post");
        }
        postRepository.delete(post);
    }

    public boolean toggleLike(String username, UUID postId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "User not found"));
        Post post = getPostEntity(postId);

        Optional<PostLike> likeOpt = postLikeRepository.findByPost_IdAndUser_Username(postId, username);

        if (likeOpt.isPresent()) {
            postLikeRepository.delete(likeOpt.get());
            post.setNumberOfLikes(Math.max(0, post.getNumberOfLikes() - 1));
            postRepository.save(post);
            return false;
        }
        else {
            PostLike like = new PostLike();
            like.setUser(user);
            like.setPost(post);
            postLikeRepository.save(like);

            post.setNumberOfLikes(post.getNumberOfLikes() + 1);
            postRepository.save(post);
            return true;
        }
    }

    public List<User> getLikesList(UUID postId) {
        getPostEntity(postId);
        return postLikeRepository.findAllByPost_Id(postId).stream()
                .map(PostLike::getUser)
                .toList();
    }

    public long getLikesCount(UUID postId) {
        getPostEntity(postId);
        return postLikeRepository.countByPost_Id(postId);
    }

    public PostComment addComment(String username, UUID postId, String comment) {
        if (comment == null || comment.isBlank()) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Comment cannot be empty");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "User not found"));
        Post post = getPostEntity(postId);

        PostComment newComment = new PostComment();
        newComment.setUser(user);
        newComment.setPost(post);
        newComment.setComment(comment);
        postCommentRepository.save(newComment);

        post.setNumberOfComments(post.getNumberOfComments() + 1);
        postRepository.save(post);

        return newComment;
    }

    public List<PostComment> getComments(UUID postId) {
        getPostEntity(postId);
        return postCommentRepository.findAllByPost_Id(postId);
    }

    public long getCommentsCount(UUID postId) {
        getPostEntity(postId);
        return postCommentRepository.findAllByPost_Id(postId).size();
    }

    private Post getPostEntity(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Post not found"));
    }

    private boolean isLikedByUser(UUID postId, String username) {
        return postLikeRepository.findByPost_IdAndUser_Username(postId, username).isPresent();
    }

    private boolean isFollowing(String followerUsername, String followeeUsername) {
        User follower = userRepository.findByUsername(followerUsername)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Follower not found"));
        User followee = userRepository.findByUsername(followeeUsername)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Followee not found"));
        return followRequestRepository.existsByFollowerAndFolloweeAndStatus(follower, followee, FollowRequestStatus.ACCEPTED);
    }

    private PostDto toDto(Post post, boolean likedByUser) {
        UserDto authorDto = new UserDto();
        authorDto.setId(post.getAuthor().getId());
        authorDto.setFirstName(post.getAuthor().getFirstName());
        authorDto.setLastName(post.getAuthor().getLastName());
        authorDto.setUsername(post.getAuthor().getUsername());

        return new PostDto(
                post.getId(),
                authorDto,
                post.getContent(),
                post.getImageUrl(),
                post.getCreatedAt().toString(),
                post.getVisibility(),
                post.getNumberOfLikes(),
                post.getNumberOfComments(),
                likedByUser
        );
    }
}
