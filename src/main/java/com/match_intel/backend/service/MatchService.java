package com.match_intel.backend.service;

import com.match_intel.backend.dto.response.CreateMatchResponse;
import com.match_intel.backend.entity.*;
import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.repository.FollowRequestRepository;
import com.match_intel.backend.repository.MatchRepository;
import com.match_intel.backend.repository.PointRepository;
import com.match_intel.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

        Match match = new Match();
        match.setPlayer1(player1);
        match.setPlayer2(player2);
        match.setInitialServer(initialServer);
        match.setStartTime(LocalDateTime.now());
        match.setVisibility(visibility);
        matchRepository.save(match);

        CreateMatchResponse responseDto = new CreateMatchResponse();
        responseDto.setMatchId(match.getId().toString());
        responseDto.setStartTime(match.getStartTime().toString());
        return responseDto;
    }

    public void addPoint(UUID matchId, String scoringPlayerUsername) {
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

            Point newPoint = new Point(parentPoint, scoringPlayerNumber);
            pointRepository.save(newPoint);

            if (newPoint.getPlayer1Sets() == 2 || newPoint.getPlayer2Sets() == 2) {
                match.setFinished(true);
                match.setFinalScore(newPoint.getPlayer1Sets() + ":" + newPoint.getPlayer2Sets());
                matchRepository.save(match);
            }
        }
        // if this is the first point
        else {
            int playerToServeNumber = match.getInitialServer().equals(match.getPlayer1().getUsername()) ? 2 : 1;
            Point newPoint = new Point(match.getId(), scoringPlayerNumber, playerToServeNumber);
            pointRepository.save(newPoint);
        }
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

        return matchRepository.findVisibleMatchesOfFollowees(requester.getId());
    }
}
