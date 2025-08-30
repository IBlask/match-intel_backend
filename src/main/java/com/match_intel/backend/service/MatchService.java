package com.match_intel.backend.service;

import com.match_intel.backend.dto.response.CreateMatchResponse;
import com.match_intel.backend.entity.*;
import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MatchService {

    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PointRepository pointRepository;
    @Autowired
    private FollowService followService;
    @Autowired
    private FollowRequestRepository followRequestRepository;
    @Autowired
    private LikeRepository likeRepository;
    @Autowired
    private CommentRepository commentRepository;


    public CreateMatchResponse createMatch(String username1, String username2, String initialServer, MatchVisibility visibility) {
        User player1 = userRepository.findByUsername(username1)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Player1 not found"));
        User player2 = userRepository.findByUsername(username2)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Player2 not found"));
        if (!initialServer.equals(player1.getUsername()) && !initialServer.equals(player2.getUsername())) {
            throw new ClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid initial server. Initial server must be one of the players!"
            );
        }

        LocalDateTime dateTimeStarted = LocalDateTime.now();

        Match match = new Match();
        match.setPlayer1(player1);
        match.setPlayer2(player2);
        match.setInitialServer(initialServer);
        match.setStartDate(dateTimeStarted.format(DateTimeFormatter.ofPattern("dd.MM.yyyy.")));
        match.setStartTime(dateTimeStarted.format(DateTimeFormatter.ofPattern("HH:mm")));
        match.setVisibility(visibility);
        matchRepository.save(match);

        CreateMatchResponse responseDto = new CreateMatchResponse();
        responseDto.setMatchId(match.getId().toString());
        responseDto.setStartTime(match.getStartTime().toString());
        return responseDto;
    }

    public Point addPoint(UUID matchId, String scoringPlayerUsername, Boolean forced) {
        if (scoringPlayerUsername == null) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Please provide scoring player username.");
        }
        if (forced == null) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Please provide whether the point is forced or not.");
        }

        Point newPoint;

        Optional<Match> matchOpt = matchRepository.findById(matchId);
        if (matchOpt.isEmpty()) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Match not found!");
        }
        Match match = matchOpt.get();

        if (match.isFinished()) {
            throw new ClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "The match is already finished. You can't add points to this match."
            );
        }

        if (!scoringPlayerUsername.equals(match.getPlayer1().getUsername())
                && !scoringPlayerUsername.equals(match.getPlayer2().getUsername())
        ) {
            throw new ClientErrorException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid scoring player username."
            );
        }

        int scoringPlayerNumber = match.getPlayer1().getUsername().equals(scoringPlayerUsername) ? 1 : 2;

        // if match is already in the progress / if the score is not 0:0
        Optional<Point> parentPointOpt = pointRepository.findTopByMatchIdOrderByCreatedAtDesc(matchId);
        if (parentPointOpt.isPresent()) {
            Point parentPoint = parentPointOpt.get();

            newPoint = new Point(parentPoint, scoringPlayerNumber, forced, scoringPlayerUsername);
            pointRepository.save(newPoint);

            // Update sets scores
            if (newPoint.getPlayer1Points().equals("0") && newPoint.getPlayer2Points().equals("0")) {
                switch (newPoint.getPlayer1Sets() + newPoint.getPlayer2Sets()) {
                    case 0 -> {
                        match.setSet1Score(newPoint.getPlayer1Games() + " : " + newPoint.getPlayer2Games());
                    }
                    case 1 -> {
                        if (newPoint.getPlayer1Games() == 0 && newPoint.getPlayer2Games() == 0) {
                            String[] splitScore = match.getSet1Score().split("\\s*:\\s*");
                            splitScore[scoringPlayerNumber-1] = String.valueOf(Integer.parseInt(splitScore[scoringPlayerNumber-1]) + 1);
                            match.setSet1Score(splitScore[0] + " : " + splitScore[1]);
                        }
                        else {
                            match.setSet2Score(newPoint.getPlayer1Games() + " : " + newPoint.getPlayer2Games());
                        }
                    }
                    case 2 -> {
                        if (newPoint.getPlayer1Games() == 0 && newPoint.getPlayer2Games() == 0) {
                            String[] splitScore = match.getSet2Score().split("\\s*:\\s*");
                            splitScore[scoringPlayerNumber-1] = String.valueOf(Integer.parseInt(splitScore[scoringPlayerNumber-1]) + 1);
                            match.setSet2Score(splitScore[0] + " : " + splitScore[1]);
                        }
                        else {
                            match.setSet3Score(newPoint.getPlayer1Games() + " : " + newPoint.getPlayer2Games());
                        }
                    }
                    case 3 -> {
                        String[] splitScore = match.getSet3Score().split("\\s*:\\s*");
                        splitScore[scoringPlayerNumber-1] = String.valueOf(Integer.parseInt(splitScore[scoringPlayerNumber-1]) + 1);
                        match.setSet3Score(splitScore[0] + " : " + splitScore[1]);
                    }
                }
            }

            int player1Efficiency = 0;
            int player2Efficiency = 0;

            if (newPoint.getPlayer1Sets() == 2 || newPoint.getPlayer2Sets() == 2) {
                match.setFinished(true);

                // Calculate efficiency points for both players
                int player1ForceCount = pointRepository.countByMatchIdAndPlayerWhoScoredAndForced(
                        match.getId(),
                        match.getPlayer1().getUsername(),
                        true
                );
                int player2ForceCount = pointRepository.countByMatchIdAndPlayerWhoScoredAndForced(
                        match.getId(),
                        match.getPlayer2().getUsername(),
                        true
                );
                int player1UnforcedCount = pointRepository.countByMatchIdAndPlayerWhoScoredAndForced(
                        match.getId(),
                        match.getPlayer1().getUsername(),
                        false
                );
                int player2UnforcedCount = pointRepository.countByMatchIdAndPlayerWhoScoredAndForced(
                        match.getId(),
                        match.getPlayer2().getUsername(),
                        false
                );

                int player1EfficiencyPoints = (player1ForceCount * 2) + player1UnforcedCount;
                int player2EfficiencyPoints = (player2ForceCount * 2) + player2UnforcedCount;

                int totalEfficiencyPoints = player1EfficiencyPoints + player2EfficiencyPoints;
                player1Efficiency = totalEfficiencyPoints == 0 ? 0 : (player1EfficiencyPoints * 100) / totalEfficiencyPoints;
                player2Efficiency = totalEfficiencyPoints == 0 ? 0 : 100 - player1Efficiency;
            }

            match.setPlayer1Efficiency(player1Efficiency);
            match.setPlayer2Efficiency(player2Efficiency);
            match.setFinalScore(newPoint.getPlayer1Sets() + " : " + newPoint.getPlayer2Sets());
            matchRepository.save(match);
        }
        // if this is the first point
        else {
            int playerToServeNumber = match.getInitialServer().equals(match.getPlayer1().getUsername()) ? 2 : 1;
            newPoint = new Point(match.getId(), scoringPlayerNumber, playerToServeNumber, forced, scoringPlayerUsername);
            pointRepository.save(newPoint);
        }

        return newPoint;
    }

    public List<Match> getVisibleMatches(String requesterUsername, String targetUsername) {
        User requester = userRepository.findByUsername(requesterUsername)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Requester not found"));
        User target = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Target user not found"));

        List<Match> allMatches = matchRepository.findByPlayer1OrPlayer2(target, target);

        return allMatches.stream().filter(match -> {
            switch (match.getVisibility()) {
                case PUBLIC -> { return true; }
                case FOLLOWERS -> {
                    return followService.isFollowing(requester.getUsername(), target.getUsername());
                }
                case PRIVATE -> {
                    return false;
                }
            }
            return false;
        }).toList();
    }

    public List<Match> getVisibleMatchesFromFollowedUsers(String requesterUsername) {
        User requester = userRepository.findByUsername(requesterUsername)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Requester not found"));    

        List<Match> matches = matchRepository.findVisibleMatchesOfFollowees(requester.getId());
        matches.forEach(match -> {
            match.setLikedByUser(likeRepository.existsByUser_UsernameAndMatch_Id(requesterUsername, match.getId()));
        });

        return matches;
    }

    public Match getMatch(UUID matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Match not found"));

        Point lastPoint = pointRepository.findTopByMatchIdOrderByCreatedAtDesc(matchId)
                .orElse(null);

        if (lastPoint == null) {
            match.setMatchStats(null);
            return match;
        }

        MatchStats matchStats = new MatchStats();
        matchStats.setPoint(lastPoint);

        int[][] forcedErrors = new int[][]{{0,0,0},{0,0,0}}; // [player][set]
        int[][] unforcedErrors = new int[][]{{0,0,0},{0,0,0}}; // [player][set]

        List<Point> allPoints = pointRepository.findAllByMatchId(matchId);
        allPoints.forEach( point -> {
            int errorPlayerIndex = match.getPlayer1().getUsername().equals(point.getPlayerWhoScored()) ? 1 : 0;
            int numberOfSets = point.getPlayer1Sets() + point.getPlayer2Sets();
            if (point.getPlayer1Games()
                    + point.getPlayer2Games()
                    + Integer.parseInt(point.getPlayer1Points())
                    + Integer.parseInt(point.getPlayer2Points())  == 0) {
                numberOfSets -= 1;
            }
            if (point.isForced()) {
                forcedErrors[errorPlayerIndex][numberOfSets]++;
            }
            else {
                unforcedErrors[errorPlayerIndex][numberOfSets]++;
            }
        });

        matchStats.setPlayer1Set1UnforcedErrors(unforcedErrors[0][0]);
        matchStats.setPlayer1Set2UnforcedErrors(unforcedErrors[0][1]);
        matchStats.setPlayer1Set3UnforcedErrors(unforcedErrors[0][2]);
        matchStats.setPlayer1Set1ForcedErrors(forcedErrors[0][0]);
        matchStats.setPlayer1Set2ForcedErrors(forcedErrors[0][1]);
        matchStats.setPlayer1Set3ForcedErrors(forcedErrors[0][2]);
        matchStats.setPlayer2Set1UnforcedErrors(unforcedErrors[1][0]);
        matchStats.setPlayer2Set2UnforcedErrors(unforcedErrors[1][1]);
        matchStats.setPlayer2Set3UnforcedErrors(unforcedErrors[1][2]);
        matchStats.setPlayer2Set1ForcedErrors(forcedErrors[1][0]);
        matchStats.setPlayer2Set2ForcedErrors(forcedErrors[1][1]);
        matchStats.setPlayer2Set3ForcedErrors(forcedErrors[1][2]);

        // Efficiency calculation
        int player1Set1EfficiencyPoints = matchStats.getPlayer2Set1ForcedErrors() * 2 + matchStats.getPlayer2Set1UnforcedErrors();
        int player2Set1EfficiencyPoints = matchStats.getPlayer1Set1ForcedErrors() * 2 + matchStats.getPlayer1Set1UnforcedErrors();
        int totalSet1EfficiencyPoints = player1Set1EfficiencyPoints + player2Set1EfficiencyPoints;
        matchStats.setPlayer1Set1Efficiency(totalSet1EfficiencyPoints == 0 ? 0 : (player1Set1EfficiencyPoints * 100) / totalSet1EfficiencyPoints);
        matchStats.setPlayer2Set1Efficiency(totalSet1EfficiencyPoints == 0 ? 0 : 100 - matchStats.getPlayer1Set1Efficiency());

        int player1Set2EfficiencyPoints = matchStats.getPlayer2Set2ForcedErrors() * 2 + matchStats.getPlayer2Set2UnforcedErrors();
        int player2Set2EfficiencyPoints = matchStats.getPlayer1Set2ForcedErrors() * 2 + matchStats.getPlayer1Set2UnforcedErrors();
        int totalSet2EfficiencyPoints = player1Set2EfficiencyPoints + player2Set2EfficiencyPoints;
        matchStats.setPlayer1Set2Efficiency(totalSet2EfficiencyPoints == 0 ? 0 : (player1Set2EfficiencyPoints * 100) / totalSet2EfficiencyPoints);
        matchStats.setPlayer2Set2Efficiency(totalSet2EfficiencyPoints == 0 ? 0 : 100 - matchStats.getPlayer1Set2Efficiency());

        int player1Set3EfficiencyPoints = matchStats.getPlayer2Set3ForcedErrors() * 2 + matchStats.getPlayer2Set3UnforcedErrors();
        int player2Set3EfficiencyPoints = matchStats.getPlayer1Set3ForcedErrors() * 2 + matchStats.getPlayer1Set3UnforcedErrors();
        int totalSet3EfficiencyPoints = player1Set3EfficiencyPoints + player2Set3EfficiencyPoints;
        matchStats.setPlayer1Set3Efficiency(totalSet3EfficiencyPoints == 0 ? 0 : (player1Set3EfficiencyPoints * 100) / totalSet3EfficiencyPoints);
        matchStats.setPlayer2Set3Efficiency(totalSet3EfficiencyPoints == 0 ? 0 : 100 - matchStats.getPlayer1Set3Efficiency());

        match.setMatchStats(matchStats);
        return match;
    }

    public boolean likeMatch(String username, UUID matchId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "User not found"));
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Match not found"));

        Optional<Like> likeOpt = likeRepository.findByMatch_IdAndUser_Username(matchId, username);

        if (likeOpt.isPresent()) {
            likeRepository.delete(likeOpt.get());
            match.setNumberOfLikes(match.getNumberOfLikes() - 1);
            matchRepository.save(match);
            return false;
        }
        else {
            Like like = new Like();
            like.setUser(user);
            like.setMatch(match);
            likeRepository.save(like);

            match.setNumberOfLikes(match.getNumberOfLikes() + 1);
            matchRepository.save(match);
            return true;
        }
    }

    public long getLikesCount(UUID matchId) {
        if (matchRepository.findById(matchId).isEmpty()) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Match not found");
        }

        return likeRepository.countByMatch_Id(matchId);
    }

    public List<User> getLikesList(UUID matchId) {
        if (matchRepository.findById(matchId).isEmpty()) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Match not found");
        }

        List<Like> likes = likeRepository.findAllByMatch_Id(matchId);
        return likes.stream().map(Like::getUser).toList();
    }

    public void commentMatch(String username, UUID matchId, String comment) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "User not found"));
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Match not found"));

        Comment newComment = new Comment();
        newComment.setMatch(match);
        newComment.setUser(user);
        newComment.setComment(comment);
        commentRepository.save(newComment);

        match.setNumberOfComments(match.getNumberOfComments() + 1);
        matchRepository.save(match);
    }

    public List<Comment> getComments(UUID matchId) {
        return commentRepository.findAllByMatch_Id(matchId);
    }

    public Long getCommentsCount(UUID matchUUID) {
        return (long) commentRepository.findAllByMatch_Id(matchUUID).size();
    }
}
