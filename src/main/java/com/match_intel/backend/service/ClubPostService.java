package com.match_intel.backend.service;

import com.match_intel.backend.dto.response.ClubPostCommentDto;
import com.match_intel.backend.dto.response.ClubPostDto;
import com.match_intel.backend.entity.*;
import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClubPostService {

    @Autowired
    private ClubPostRepository clubPostRepository;
    @Autowired
    private ClubPostLikeRepository clubPostLikeRepository;
    @Autowired
    private ClubPostCommentRepository clubPostCommentRepository;
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ClubService clubService;

    public ClubPostDto createPost(String username, UUID clubId, String content, String imageUrl) {
        if (content == null || content.isBlank()) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Post content cannot be empty");
        }
        if (!clubService.isClubAdmin(username, clubId)) {
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "Only club admins can create club posts");
        }

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.NOT_FOUND, "Club not found"));

        ClubPost post = new ClubPost();
        post.setClub(club);
        post.setContent(content);
        post.setImageUrl(imageUrl);
        clubPostRepository.save(post);

        return toDto(post, false);
    }

    public List<ClubPostDto> getClubPosts(UUID clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.NOT_FOUND, "Club not found"));
        return clubPostRepository.findByClubOrderByCreatedAtDesc(club).stream()
                .map(post -> toDto(post, false))
                .toList();
    }

    public List<ClubPostDto> getFollowedClubPosts(String username) {
        User requester = userRepository.findByUsername(username)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Requester not found"));

        return clubPostRepository.findVisiblePostsOfFollowedClubs(requester.getId()).stream()
                .map(post -> toDto(post, isLikedByUser(post.getId(), username)))
                .toList();
    }

    public ClubPostDto getPost(String username, UUID postId) {
        ClubPost post = getPostEntity(postId);
        return toDto(post, isLikedByUser(postId, username));
    }

    public void deletePost(String username, UUID postId) {
        ClubPost post = getPostEntity(postId);
        if (!clubService.isClubAdmin(username, post.getClub().getId())) {
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "Only club admins can delete club posts");
        }
        clubPostLikeRepository.findAllByPost_Id(postId).forEach(clubPostLikeRepository::delete);
        clubPostCommentRepository.findAllByPost_Id(postId).forEach(clubPostCommentRepository::delete);
        clubPostRepository.delete(post);
    }

    public boolean toggleLike(String username, UUID postId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "User not found"));
        ClubPost post = getPostEntity(postId);

        Optional<ClubPostLike> likeOpt = clubPostLikeRepository.findByPost_IdAndUser_Username(postId, username);

        if (likeOpt.isPresent()) {
            clubPostLikeRepository.delete(likeOpt.get());
            post.setNumberOfLikes(Math.max(0, post.getNumberOfLikes() - 1));
            clubPostRepository.save(post);
            return false;
        } else {
            ClubPostLike like = new ClubPostLike();
            like.setUser(user);
            like.setPost(post);
            clubPostLikeRepository.save(like);

            post.setNumberOfLikes(post.getNumberOfLikes() + 1);
            clubPostRepository.save(post);
            return true;
        }
    }

    public List<User> getLikesList(UUID postId) {
        getPostEntity(postId);
        return clubPostLikeRepository.findAllByPost_Id(postId).stream()
                .map(ClubPostLike::getUser)
                .toList();
    }

    public long getLikesCount(UUID postId) {
        getPostEntity(postId);
        return clubPostLikeRepository.countByPost_Id(postId);
    }

    public ClubPostComment addComment(String username, UUID postId, String comment) {
        if (comment == null || comment.isBlank()) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Comment cannot be empty");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "User not found"));
        ClubPost post = getPostEntity(postId);

        ClubPostComment newComment = new ClubPostComment();
        newComment.setUser(user);
        newComment.setPost(post);
        newComment.setComment(comment);
        clubPostCommentRepository.save(newComment);

        post.setNumberOfComments(post.getNumberOfComments() + 1);
        clubPostRepository.save(post);

        return newComment;
    }

    public List<ClubPostComment> getComments(UUID postId) {
        getPostEntity(postId);
        return clubPostCommentRepository.findAllByPost_Id(postId);
    }

    public long getCommentsCount(UUID postId) {
        getPostEntity(postId);
        return clubPostCommentRepository.countByPost_Id(postId);
    }

    public ClubPost getPostEntity(UUID postId) {
        return clubPostRepository.findById(postId)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Club post not found"));
    }

    private boolean isLikedByUser(UUID postId, String username) {
        return clubPostLikeRepository.findByPost_IdAndUser_Username(postId, username).isPresent();
    }

    private ClubPostDto toDto(ClubPost post, boolean likedByUser) {
        return new ClubPostDto(
                post.getId(),
                post.getClub().getId(),
                post.getClub().getName(),
                post.getContent(),
                post.getImageUrl(),
                post.getCreatedAt(),
                post.getNumberOfLikes(),
                post.getNumberOfComments(),
                likedByUser
        );
    }

    public static ClubPostCommentDto toCommentDto(ClubPostComment comment) {
        return new ClubPostCommentDto(
                comment.getId(),
                comment.getUser().getUsername(),
                comment.getUser().getFirstName(),
                comment.getUser().getLastName(),
                comment.getComment(),
                comment.getCreatedAt()
        );
    }
}
