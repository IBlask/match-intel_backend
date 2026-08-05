package com.match_intel.backend.service;

import com.match_intel.backend.dto.response.PostDto;
import com.match_intel.backend.entity.*;
import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.repository.FollowRequestRepository;
import com.match_intel.backend.repository.PostCommentRepository;
import com.match_intel.backend.repository.PostLikeRepository;
import com.match_intel.backend.repository.PostRepository;
import com.match_intel.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PostServiceUnitTests {

    @Mock
    private PostRepository postRepository;
    @Mock
    private PostLikeRepository postLikeRepository;
    @Mock
    private PostCommentRepository postCommentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FollowRequestRepository followRequestRepository;
    @InjectMocks
    private PostService postService;

    private User author;
    private User follower;
    private Post post;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        author = new User("author1", "Ann", "Author", "ann@example.com", "pass123");
        follower = new User("follower1", "Fay", "Follower", "fay@example.com", "pass123");

        post = new Post();
        post.setAuthor(author);
        post.setContent("Hello world");
        post.setVisibility(MatchVisibility.PUBLIC);
    }

    @Test
    @DisplayName("createPost - creates post with author")
    void createPost_shouldCreatePost() {
        when(userRepository.findByUsername("author1")).thenReturn(Optional.of(author));

        PostDto result = postService.createPost("author1", "Great match!", null, MatchVisibility.PUBLIC);

        assertNotNull(result);
        assertEquals("Great match!", result.content());
        assertEquals("author1", result.author().getUsername());
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("createPost - empty content throws")
    void createPost_shouldRejectEmptyContent() {
        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                postService.createPost("author1", "   ", null, MatchVisibility.PUBLIC));

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode());
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    @DisplayName("getFollowedPosts - returns posts of followed users")
    void getFollowedPosts_shouldReturnFollowedPosts() {
        when(userRepository.findByUsername("follower1")).thenReturn(Optional.of(follower));
        when(postRepository.findVisiblePostsOfFollowees(follower.getId())).thenReturn(List.of(post));
        when(postLikeRepository.findByPost_IdAndUser_Username(post.getId(), "follower1"))
                .thenReturn(Optional.empty());

        List<PostDto> result = postService.getFollowedPosts("follower1");

        assertEquals(1, result.size());
        assertEquals("Hello world", result.get(0).content());
    }

    @Test
    @DisplayName("getVisiblePosts - PRIVATE posts hidden from other users")
    void getVisiblePosts_shouldHidePrivatePostsFromOthers() {
        post.setVisibility(MatchVisibility.PRIVATE);
        when(userRepository.findByUsername("follower1")).thenReturn(Optional.of(follower));
        when(userRepository.findByUsername("author1")).thenReturn(Optional.of(author));
        when(postRepository.findByAuthor_UsernameOrderByCreatedAtDesc("author1")).thenReturn(List.of(post));

        List<PostDto> result = postService.getVisiblePosts("follower1", "author1");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getVisiblePosts - PRIVATE posts visible to own profile")
    void getVisiblePosts_shouldShowPrivatePostsOnOwnProfile() {
        post.setVisibility(MatchVisibility.PRIVATE);
        when(userRepository.findByUsername("author1")).thenReturn(Optional.of(author));
        when(postRepository.findByAuthor_UsernameOrderByCreatedAtDesc("author1")).thenReturn(List.of(post));

        List<PostDto> result = postService.getVisiblePosts("author1", "author1");

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("getVisiblePosts - PRIVATE profile returns no posts for others")
    void getVisiblePosts_shouldReturnEmptyForPrivateProfile() {
        author.setProfileVisibility(ProfileVisibility.PRIVATE);
        when(userRepository.findByUsername("follower1")).thenReturn(Optional.of(follower));
        when(userRepository.findByUsername("author1")).thenReturn(Optional.of(author));

        List<PostDto> result = postService.getVisiblePosts("follower1", "author1");

        assertTrue(result.isEmpty());
        verify(postRepository, never()).findByAuthor_UsernameOrderByCreatedAtDesc(anyString());
    }

    @Test
    @DisplayName("toggleLike - likes and unlikes")
    void toggleLike_shouldToggle() {
        post.setId(UUID.randomUUID());
        when(userRepository.findByUsername("follower1")).thenReturn(Optional.of(follower));
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(postLikeRepository.findByPost_IdAndUser_Username(post.getId(), "follower1"))
                .thenReturn(Optional.empty());

        assertTrue(postService.toggleLike("follower1", post.getId()));
        assertEquals(1, post.getNumberOfLikes());

        PostLike like = new PostLike();
        like.setUser(follower);
        like.setPost(post);
        when(postLikeRepository.findByPost_IdAndUser_Username(post.getId(), "follower1"))
                .thenReturn(Optional.of(like));

        assertFalse(postService.toggleLike("follower1", post.getId()));
        assertEquals(0, post.getNumberOfLikes());
    }

    @Test
    @DisplayName("deletePost - only author can delete")
    void deletePost_shouldOnlyAllowAuthor() {
        post.setId(UUID.randomUUID());
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                postService.deletePost("follower1", post.getId()));

        assertEquals(HttpStatus.FORBIDDEN.value(), exception.getStatusCode());
        verify(postRepository, never()).delete(any(Post.class));

        postService.deletePost("author1", post.getId());
        verify(postRepository).delete(post);
    }

    @Test
    @DisplayName("addComment - adds comment and increments count")
    void addComment_shouldAddAndIncrementCount() {
        post.setId(UUID.randomUUID());
        when(userRepository.findByUsername("follower1")).thenReturn(Optional.of(follower));
        when(postRepository.findById(post.getId())).thenReturn(Optional.of(post));

        PostComment comment = postService.addComment("follower1", post.getId(), "Nice!");

        assertNotNull(comment);
        assertEquals("Nice!", comment.getComment());
        assertEquals("follower1", comment.getUser().getUsername());
        assertEquals(1, post.getNumberOfComments());
        verify(postCommentRepository).save(any(PostComment.class));
    }
}
