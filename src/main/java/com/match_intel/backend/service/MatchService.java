package com.match_intel.backend.service;

import com.match_intel.backend.dto.response.CreateMatchResponse;
import com.match_intel.backend.entity.Match;
import com.match_intel.backend.entity.User;
import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.repository.MatchRepository;
import com.match_intel.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MatchService {

    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private UserRepository userRepository;


    public CreateMatchResponse createMatch(String username1, String username2, String initialServer) {
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
        matchRepository.save(match);

        CreateMatchResponse responseDto = new CreateMatchResponse();
        responseDto.setMatchId(match.getId().toString());
        responseDto.setStartTime(match.getStartTime().toString());
        return responseDto;
    }
}
