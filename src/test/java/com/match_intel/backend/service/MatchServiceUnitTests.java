package com.match_intel.backend.service;

import com.match_intel.backend.dto.response.CreateMatchResponse;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MatchServiceUnitTests {

    @Mock
    private MatchRepository matchRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PointRepository pointRepository;
    @Mock
    private FollowService followService;
    @Mock
    private FollowRequestRepository followRequestRepository;
    @Mock
    private LikeRepository likeRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ClubRepository clubRepository;
    @InjectMocks
    private MatchService matchService;

    private User player1;
    private User player2;
    private User referee;
    private Match match;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        player1 = new User("p1", "Player", "One", "p1@example.com", "pass123");
        player2 = new User("p2", "Player", "Two", "p2@example.com", "pass123");
        referee = new User("ref1", "Ref", "Eree", "ref1@example.com", "pass123");

        match = new Match();
        match.setPlayer1(player1);
        match.setPlayer2(player2);
        match.setInitialServer("p1");
        match.setVisibility(MatchVisibility.PUBLIC);
    }

    @Test
    @DisplayName("createMatchAsReferee - creates match with referee set")
    void createMatchAsReferee_shouldCreateMatchWithReferee() {
        when(userRepository.findByUsername("ref1")).thenReturn(Optional.of(referee));
        when(userRepository.findByUsername("p1")).thenReturn(Optional.of(player1));
        when(userRepository.findByUsername("p2")).thenReturn(Optional.of(player2));
        when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> {
            Match saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                ReflectionTestUtils.setField(saved, "id", UUID.randomUUID());
            }
            return saved;
        });

        CreateMatchResponse response = matchService.createMatchAsReferee(
                "ref1", "p1", "p2", "p1", MatchVisibility.PUBLIC, null
        );

        assertNotNull(response);
        assertNotNull(response.getMatchId());
        verify(matchRepository).save(argThat(saved ->
                saved.getReferee() != null
                        && saved.getReferee().getUsername().equals("ref1")
                        && saved.getPlayer1().getUsername().equals("p1")
                        && saved.getPlayer2().getUsername().equals("p2")
        ));
    }

    @Test
    @DisplayName("createMatchAsReferee - referee cannot be a player")
    void createMatchAsReferee_shouldRejectRefereeBeingPlayer() {
        when(userRepository.findByUsername("ref1")).thenReturn(Optional.of(referee));
        when(userRepository.findByUsername("p1")).thenReturn(Optional.of(player1));
        when(userRepository.findByUsername("p2")).thenReturn(Optional.of(player2));

        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                matchService.createMatchAsReferee("p1", "p1", "p2", "p1", MatchVisibility.PUBLIC, null));

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode());
        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    @DisplayName("createMatchAsReferee - players must be different")
    void createMatchAsReferee_shouldRejectSamePlayers() {
        when(userRepository.findByUsername("ref1")).thenReturn(Optional.of(referee));
        when(userRepository.findByUsername("p1")).thenReturn(Optional.of(player1));

        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                matchService.createMatchAsReferee("ref1", "p1", "p1", "p1", MatchVisibility.PUBLIC, null));

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode());
        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    @DisplayName("addPoint - referee can add points")
    void addPoint_shouldAllowRefereeToScore() {
        match.setReferee(referee);
        when(matchRepository.findById(any(UUID.class))).thenReturn(Optional.of(match));
        when(pointRepository.findTopByMatchIdOrderByCreatedAtDesc(any(UUID.class)))
                .thenReturn(Optional.empty());

        Point point = matchService.addPoint(
                UUID.randomUUID(), "p1", true, "ref1", "FIRST"
        );

        assertNotNull(point);
        assertEquals(Boolean.TRUE, point.getIsFirstServe());
        verify(pointRepository).save(any(Point.class));
    }

    @Test
    @DisplayName("addPoint - player can add points")
    void addPoint_shouldAllowPlayerToScore() {
        when(matchRepository.findById(any(UUID.class))).thenReturn(Optional.of(match));
        when(pointRepository.findTopByMatchIdOrderByCreatedAtDesc(any(UUID.class)))
                .thenReturn(Optional.empty());

        Point point = matchService.addPoint(
                UUID.randomUUID(), "p1", true, "p1", "SECOND"
        );

        assertNotNull(point);
        assertEquals(Boolean.FALSE, point.getIsFirstServe());
    }

    @Test
    @DisplayName("addPoint - stranger cannot add points")
    void addPoint_shouldRejectStranger() {
        when(matchRepository.findById(any(UUID.class))).thenReturn(Optional.of(match));

        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                matchService.addPoint(UUID.randomUUID(), "p1", true, "stranger", null));

        assertEquals(HttpStatus.FORBIDDEN.value(), exception.getStatusCode());
        verify(pointRepository, never()).save(any(Point.class));
    }

    @Test
    @DisplayName("createMatch - with valid clubId sets club on match")
    void createMatch_shouldSetClubWhenClubIdProvided() {
        when(userRepository.findByUsername("p1")).thenReturn(Optional.of(player1));
        when(userRepository.findByUsername("p2")).thenReturn(Optional.of(player2));
        UUID clubId = UUID.randomUUID();
        Club club = new Club();
        club.setId(clubId);
        club.setName("Test Club");
        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
        when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> {
            Match saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                ReflectionTestUtils.setField(saved, "id", UUID.randomUUID());
            }
            return saved;
        });

        CreateMatchResponse response = matchService.createMatch(
                "p1", "p2", "p1", MatchVisibility.PUBLIC, clubId
        );

        assertNotNull(response);
        verify(matchRepository).save(argThat(saved ->
                saved.getClub() != null && saved.getClub().getId().equals(clubId)
        ));
    }

    @Test
    @DisplayName("createMatch - with unknown clubId throws 400")
    void createMatch_shouldRejectUnknownClub() {
        when(userRepository.findByUsername("p1")).thenReturn(Optional.of(player1));
        when(userRepository.findByUsername("p2")).thenReturn(Optional.of(player2));
        UUID clubId = UUID.randomUUID();
        when(clubRepository.findById(clubId)).thenReturn(Optional.empty());

        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                matchService.createMatch("p1", "p2", "p1", MatchVisibility.PUBLIC, clubId));

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode());
        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    @DisplayName("createMatchAsReferee - with valid clubId sets club on match")
    void createMatchAsReferee_shouldSetClubWhenClubIdProvided() {
        when(userRepository.findByUsername("ref1")).thenReturn(Optional.of(referee));
        when(userRepository.findByUsername("p1")).thenReturn(Optional.of(player1));
        when(userRepository.findByUsername("p2")).thenReturn(Optional.of(player2));
        UUID clubId = UUID.randomUUID();
        Club club = new Club();
        club.setId(clubId);
        club.setName("Test Club");
        when(clubRepository.findById(clubId)).thenReturn(Optional.of(club));
        when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> {
            Match saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                ReflectionTestUtils.setField(saved, "id", UUID.randomUUID());
            }
            return saved;
        });

        CreateMatchResponse response = matchService.createMatchAsReferee(
                "ref1", "p1", "p2", "p1", MatchVisibility.PUBLIC, clubId
        );

        assertNotNull(response);
        verify(matchRepository).save(argThat(saved ->
                saved.getClub() != null && saved.getClub().getId().equals(clubId)
        ));
    }

    @Test
    @DisplayName("createMatchAsReferee - with unknown clubId throws 400")
    void createMatchAsReferee_shouldRejectUnknownClub() {
        when(userRepository.findByUsername("ref1")).thenReturn(Optional.of(referee));
        when(userRepository.findByUsername("p1")).thenReturn(Optional.of(player1));
        when(userRepository.findByUsername("p2")).thenReturn(Optional.of(player2));
        UUID clubId = UUID.randomUUID();
        when(clubRepository.findById(clubId)).thenReturn(Optional.empty());

        ClientErrorException exception = assertThrows(ClientErrorException.class, () ->
                matchService.createMatchAsReferee(
                        "ref1", "p1", "p2", "p1", MatchVisibility.PUBLIC, clubId
                ));

        assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode());
        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    @DisplayName("getMatch - computes first and second serve stats")
    void getMatch_shouldComputeServeStats() {
        UUID matchId = UUID.randomUUID();

        // p1 serves: first serve made (point to p1)
        Point p1FirstMade = new Point(matchId, 1, 1, false, "p1");
        p1FirstMade.setIsFirstServe(true);
        // p1 serves: first serve missed (point to p2)
        Point p1FirstMissed = new Point(matchId, 2, 1, false, "p2");
        p1FirstMissed.setIsFirstServe(true);
        // p1 serves: second serve made (point to p1)
        Point p1SecondMade = new Point(matchId, 1, 1, false, "p1");
        p1SecondMade.setIsFirstServe(false);
        // p2 serves: first serve made (point to p2)
        Point p2FirstMade = new Point(matchId, 2, 2, false, "p2");
        p2FirstMade.setIsFirstServe(true);
        // rally point without serve info (should be ignored)
        Point rally = new Point(matchId, 2, 2, false, "p2");
        rally.setIsFirstServe(null);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(pointRepository.findTopByMatchIdOrderByCreatedAtDesc(matchId))
                .thenReturn(Optional.of(p2FirstMade));
        when(pointRepository.findAllByMatchId(matchId))
                .thenReturn(List.of(p1FirstMade, p1FirstMissed, p1SecondMade, p2FirstMade, rally));

        Match result = matchService.getMatch(matchId);

        MatchStats stats = result.getMatchStats();
        assertNotNull(stats);
        assertEquals(2, stats.getPlayer1FirstServesAttempted());
        assertEquals(1, stats.getPlayer1FirstServesMade());
        assertEquals(50.0, stats.getPlayer1FirstServePercentage());
        assertEquals(1, stats.getPlayer1SecondServesAttempted());
        assertEquals(1, stats.getPlayer1SecondServesMade());
        assertEquals(100.0, stats.getPlayer1SecondServePercentage());
        assertEquals(1, stats.getPlayer2FirstServesAttempted());
        assertEquals(1, stats.getPlayer2FirstServesMade());
        assertEquals(100.0, stats.getPlayer2FirstServePercentage());
        assertNull(stats.getPlayer2SecondServesAttempted());
        assertNull(stats.getPlayer2SecondServePercentage());
    }

    @Test
    @DisplayName("getMatch - old points without serve info do not crash")
    void getMatch_shouldNotCrashWhenServeInfoMissing() {
        UUID matchId = UUID.randomUUID();

        Point oldPoint = new Point(matchId, 1, 1, false, "p1");
        oldPoint.setIsFirstServe(null);

        when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
        when(pointRepository.findTopByMatchIdOrderByCreatedAtDesc(matchId))
                .thenReturn(Optional.of(oldPoint));
        when(pointRepository.findAllByMatchId(matchId)).thenReturn(List.of(oldPoint));

        Match result = matchService.getMatch(matchId);

        MatchStats stats = result.getMatchStats();
        assertNotNull(stats);
        assertNull(stats.getPlayer1FirstServesAttempted());
        assertNull(stats.getPlayer1FirstServePercentage());
        assertEquals(0, stats.getPlayer1Set1FirstServesAttempted());
    }
}
