package com.match_intel.backend.service;

import com.match_intel.backend.dto.response.ClubPostDto;
import com.match_intel.backend.entity.*;
import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ClubPostServiceUnitTests {

    @Mock
    private ClubPostRepository clubPostRepository;
    @Mock
    private ClubPostLikeRepository clubPostLikeRepository;
    @Mock
    private ClubPostCommentRepository clubPostCommentRepository;
    @Mock
    private ClubRepository clubRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ClubService clubService;
    @InjectMocks
    private ClubPostService clubPostService;

    private User user;
    private Club club;
    private ClubPost post;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User("user", "User", "One", "user@example.com", "pass123");
        club = new Club();
        club.setId(UUID.randomUUID());
        club.setName("Test Club");

        post = new ClubPost();
        post.setId(UUID.randomUUID());
        post.setClub(club);
        post.setContent("Hello club!");
        post.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("createPost - admin can create a post")
    void createPost_shouldAllowAdmin() {
        when(clubService.isClubAdmin("admin", club.getId())).thenReturn(true);
        when(clubRepository.findById(club.getId())).thenReturn(Optional.of(club));
        when(clubPostRepository.save(any(ClubPost.class))).thenAnswer(invocation -> {
            ClubPost saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(UUID.randomUUID());
            }
            return saved;
        });

        ClubPostDto dto = clubPostService.createPost("admin", club.getId(), "Hello club!", null);

        assertNotNull(dto);
        assertEquals("Hello club!", dto.content());
        assertEquals(club.getName(), dto.clubName());
    }

    @Test
    @DisplayName("createPost - non-admin gets 403")
    void createPost_shouldRejectNonAdmin() {
        when(clubService.isClubAdmin("user", club.getId())).thenReturn(false);

        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                clubPostService.createPost("user", club.getId(), "Hello club!", null));

        assertEquals(HttpStatus.FORBIDDEN.value(), exception.getStatusCode());
        verify(clubPostRepository, never()).save(any(ClubPost.class));
    }

    @Test
    @DisplayName("createPost - empty content throws 400")
    void createPost_shouldRejectEmptyContent() {
        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                clubPostService.createPost("admin", club.getId(), "   ", null));

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode());
        verify(clubPostRepository, never()).save(any(ClubPost.class));
    }

    @Test
    @DisplayName("getClubPosts - returns posts of a club")
    void getClubPosts_shouldReturnClubPosts() {
        when(clubRepository.findById(club.getId())).thenReturn(Optional.of(club));
        when(clubPostRepository.findByClubOrderByCreatedAtDesc(club)).thenReturn(List.of(post));

        List<ClubPostDto> posts = clubPostService.getClubPosts(club.getId());

        assertEquals(1, posts.size());
        assertEquals(post.getId(), posts.get(0).id());
    }

    @Test
    @DisplayName("getFollowedClubPosts - returns posts of followed clubs")
    void getFollowedClubPosts_shouldReturnPosts() {
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(clubPostRepository.findVisiblePostsOfFollowedClubs(user.getId()))
                .thenReturn(List.of(post));
        when(clubPostLikeRepository.findByPost_IdAndUser_Username(post.getId(), "user"))
                .thenReturn(Optional.empty());

        List<ClubPostDto> posts = clubPostService.getFollowedClubPosts("user");

        assertEquals(1, posts.size());
        assertFalse(posts.get(0).likedByUser());
    }

    @Test
    @DisplayName("deletePost - admin can delete, non-admin gets 403")
    void deletePost_shouldBeAdminOnly() {
        when(clubPostRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(clubService.isClubAdmin("admin", club.getId())).thenReturn(true);
        when(clubService.isClubAdmin("user", club.getId())).thenReturn(false);
        when(clubPostLikeRepository.findAllByPost_Id(post.getId())).thenReturn(List.of());
        when(clubPostCommentRepository.findAllByPost_Id(post.getId())).thenReturn(List.of());

        clubPostService.deletePost("admin", post.getId());
        verify(clubPostRepository).delete(post);

        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                clubPostService.deletePost("user", post.getId()));

        assertEquals(HttpStatus.FORBIDDEN.value(), exception.getStatusCode());
    }

    @Test
    @DisplayName("toggleLike - likes then unlikes and updates count")
    void toggleLike_shouldToggleAndUpdateCount() {
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(clubPostRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(clubPostLikeRepository.findByPost_IdAndUser_Username(post.getId(), "user"))
                .thenReturn(Optional.empty());

        boolean liked = clubPostService.toggleLike("user", post.getId());

        assertTrue(liked);
        assertEquals(1, post.getNumberOfLikes());

        ClubPostLike like = new ClubPostLike();
        like.setUser(user);
        like.setPost(post);
        when(clubPostLikeRepository.findByPost_IdAndUser_Username(post.getId(), "user"))
                .thenReturn(Optional.of(like));
        post.setNumberOfLikes(1);

        boolean unliked = clubPostService.toggleLike("user", post.getId());

        assertFalse(unliked);
        assertEquals(0, post.getNumberOfLikes());
    }

    @Test
    @DisplayName("addComment - adds comment and increments count")
    void addComment_shouldAddAndIncrementCount() {
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(clubPostRepository.findById(post.getId())).thenReturn(Optional.of(post));
        when(clubPostCommentRepository.save(any(ClubPostComment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ClubPostComment comment = clubPostService.addComment("user", post.getId(), "Nice post");

        assertNotNull(comment);
        assertEquals("Nice post", comment.getComment());
        assertEquals(1, post.getNumberOfComments());
        verify(clubPostRepository).save(post);
    }

    @Test
    @DisplayName("addComment - empty comment throws 400")
    void addComment_shouldRejectEmptyComment() {
        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                clubPostService.addComment("user", post.getId(), " "));

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode());
        verify(clubPostCommentRepository, never()).save(any(ClubPostComment.class));
    }
}
