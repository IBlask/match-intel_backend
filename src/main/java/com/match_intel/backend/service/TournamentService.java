package com.match_intel.backend.service;

import com.match_intel.backend.dto.response.TournamentBracketDto;
import com.match_intel.backend.dto.response.TournamentDetailsDto;
import com.match_intel.backend.dto.response.TournamentDto;
import com.match_intel.backend.dto.response.TournamentMatchDto;
import com.match_intel.backend.dto.response.TournamentRegistrationDto;
import com.match_intel.backend.dto.response.TournamentRefereeDto;
import com.match_intel.backend.dto.response.TournamentRoundDto;
import com.match_intel.backend.dto.response.TournamentStatusDto;
import com.match_intel.backend.entity.*;
import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.repository.MatchRepository;
import com.match_intel.backend.repository.TournamentRegistrationRepository;
import com.match_intel.backend.repository.TournamentRefereeRepository;
import com.match_intel.backend.repository.TournamentRepository;
import com.match_intel.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TournamentService {

    @Autowired
    private TournamentRepository tournamentRepository;
    @Autowired
    private TournamentRegistrationRepository registrationRepository;
    @Autowired
    private TournamentRefereeRepository refereeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ClubService clubService;
    @Autowired
    private MatchRepository matchRepository;

    @Transactional
    public TournamentDto createTournament(String username, UUID clubId, String name,
                                           String startDate, String registrationDeadline,
                                           int maxPlayers) {
        if (!clubService.isClubAdmin(username, clubId)) {
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "Only club admins can create tournaments");
        }
        if (name == null || name.isBlank()) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Tournament name is required");
        }
        if (maxPlayers < 2) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Tournament must allow at least 2 players");
        }
        if (startDate == null || startDate.isBlank()) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Start date is required");
        }
        if (registrationDeadline == null || registrationDeadline.isBlank()) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Registration deadline is required");
        }

        LocalDateTime deadline;
        try {
            deadline = LocalDateTime.parse(registrationDeadline, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        } catch (Exception e) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Invalid registration deadline format");
        }
        if (deadline.isBefore(LocalDateTime.now())) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Registration deadline must be in the future");
        }

        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "User not found"));
        Club club = clubService.getClubEntity(clubId);

        Tournament tournament = new Tournament();
        tournament.setName(name.trim());
        tournament.setClub(club);
        tournament.setCreatedBy(creator);
        tournament.setStartDate(startDate);
        tournament.setRegistrationDeadline(registrationDeadline);
        tournament.setMaxPlayers(maxPlayers);
        tournament.setStatus(TournamentStatus.OPEN);
        tournamentRepository.save(tournament);

        return toDto(tournament, username);
    }

    public List<TournamentDto> getTournamentsByClub(UUID clubId, String requesterUsername) {
        return tournamentRepository.findByClubIdOrderByCreatedAtDesc(clubId).stream()
                .map(t -> toDto(t, requesterUsername))
                .toList();
    }

    public List<TournamentDto> getAllTournaments(String requesterUsername) {
        return tournamentRepository.findByStatusIn(List.of(
                TournamentStatus.OPEN, TournamentStatus.DRAWN, TournamentStatus.IN_PROGRESS
        )).stream()
                .map(t -> toDto(t, requesterUsername))
                .toList();
    }

    public TournamentDetailsDto getTournamentDetails(UUID tournamentId, String requesterUsername) {
        Tournament tournament = getTournamentEntity(tournamentId);
        TournamentDto tournamentDto = toDto(tournament, requesterUsername);

        List<TournamentRegistrationDto> registrations = registrationRepository
                .findByTournamentOrderByRegisteredAtAsc(tournament).stream()
                .map(reg -> new TournamentRegistrationDto(
                        reg.getId(),
                        reg.getUser().getUsername(),
                        reg.getUser().getFirstName(),
                        reg.getUser().getLastName(),
                        registrationRepository.findByTournamentOrderByRegisteredAtAsc(tournament).indexOf(reg) + 1,
                        reg.getRegisteredAt()
                ))
                .toList();

        return new TournamentDetailsDto(tournamentDto, registrations);
    }

    @Transactional
    public TournamentRegistrationDto registerForTournament(String username, UUID tournamentId) {
        Tournament tournament = getTournamentEntity(tournamentId);

        if (tournament.getStatus() != TournamentStatus.OPEN) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Registration is closed");
        }

        LocalDateTime deadline;
        try {
            deadline = LocalDateTime.parse(tournament.getRegistrationDeadline(),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        } catch (Exception e) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Invalid registration deadline");
        }
        if (deadline.isBefore(LocalDateTime.now())) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Registration deadline has passed");
        }

        if (tournament.getNumberOfPlayers() >= tournament.getMaxPlayers()) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Tournament is full");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "User not found"));

        if (registrationRepository.existsByTournamentAndUser(tournament, user)) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Already registered");
        }

        TournamentRegistration registration = new TournamentRegistration();
        registration.setTournament(tournament);
        registration.setUser(user);
        registrationRepository.save(registration);

        tournament.setNumberOfPlayers(tournament.getNumberOfPlayers() + 1);
        tournamentRepository.save(tournament);

        int seed = registrationRepository.findByTournamentOrderByRegisteredAtAsc(tournament).size();
        return new TournamentRegistrationDto(
                registration.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                seed,
                registration.getRegisteredAt()
        );
    }

    @Transactional
    public void unregisterFromTournament(String username, UUID tournamentId) {
        Tournament tournament = getTournamentEntity(tournamentId);

        if (tournament.getStatus() != TournamentStatus.OPEN) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Cannot unregister after tournament has started");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "User not found"));

        TournamentRegistration registration = registrationRepository.findByTournamentAndUser(tournament, user)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Not registered for this tournament"));

        registrationRepository.delete(registration);
        tournament.setNumberOfPlayers(tournament.getNumberOfPlayers() - 1);
        tournamentRepository.save(tournament);
    }

    @Transactional
    public void cancelTournament(String username, UUID tournamentId) {
        Tournament tournament = getTournamentEntity(tournamentId);

        if (!clubService.isClubAdmin(username, tournament.getClub().getId())) {
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "Only club admins can cancel tournaments");
        }
        if (tournament.getStatus() != TournamentStatus.OPEN) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Tournament can only be cancelled from OPEN status");
        }

        List<TournamentRegistration> registrations = registrationRepository
                .findByTournamentOrderByRegisteredAtAsc(tournament);
        registrationRepository.deleteAll(registrations);
        tournamentRepository.delete(tournament);
    }

    @Transactional
    public TournamentDto generateBracket(String username, UUID tournamentId) {
        Tournament tournament = getTournamentEntity(tournamentId);

        if (!clubService.isClubAdmin(username, tournament.getClub().getId())) {
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "Only club admins can generate brackets");
        }
        if (tournament.getStatus() != TournamentStatus.OPEN) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Bracket can only be generated for OPEN tournaments");
        }

        List<TournamentRegistration> registrations = registrationRepository
                .findByTournamentOrderByRegisteredAtAsc(tournament);

        if (registrations.size() < 2) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "At least 2 players required to generate a bracket");
        }

        int numPlayers = registrations.size();
        int totalSlots = nextPowerOf2(numPlayers);
        int byes = totalSlots - numPlayers;
        int totalRounds = (int) (Math.log(totalSlots) / Math.log(2));

        // Rounds 2..totalRounds: create placeholder matches FIRST so that the round-1
        // creation loop can populate bye players into round-2 slots as it goes.
        for (int round = 2; round <= totalRounds; round++) {
            int matchCount = totalSlots / (int) Math.pow(2, round);
            for (int i = 0; i < matchCount; i++) {
                Match match = new Match();
                match.setTournament(tournament);
                match.setRound(round);
                match.setBracketPosition(i);
                match.setInitialServer(""); // will be set when players are determined
                match.setStartDate(tournament.getStartDate());
                match.setStartTime("00:00");
                match.setVisibility(MatchVisibility.PUBLIC);
                match.setClub(tournament.getClub());
                // player1, player2 null — to be filled when winners are determined
                matchRepository.save(match);
            }
        }

        // Round 1: create matches
        int round1MatchCount = totalSlots / 2;
        for (int i = 0; i < round1MatchCount; i++) {
            int pos1 = i * 2;
            int pos2 = i * 2 + 1;

            int seed1 = getSeedForBracketPosition(pos1, totalSlots);
            int seed2 = getSeedForBracketPosition(pos2, totalSlots);

            boolean seed1Phantom = seed1 > numPlayers;
            boolean seed2Phantom = seed2 > numPlayers;

            if (seed1Phantom && seed2Phantom) continue;

            if (seed1Phantom || seed2Phantom) {
                // Bye match
                int presentSeed = seed1Phantom ? seed2 : seed1;
                User presentUser = registrations.get(presentSeed - 1).getUser();

                Match match = new Match();
                match.setTournament(tournament);
                match.setRound(1);
                match.setBracketPosition(i);
                match.setPlayer1(presentUser);
                match.setPlayer2(presentUser); // same player (bye)
                match.setInitialServer(presentUser.getUsername());
                match.setFinished(true);
                match.setBye(true);
                match.setFinalScore("W/O");
                match.setStartDate(tournament.getStartDate());
                match.setStartTime("00:00");
                match.setVisibility(MatchVisibility.PUBLIC);
                match.setClub(tournament.getClub());
                matchRepository.save(match);

                // Propagate the bye player into the next round's placeholder so they
                // show up in the bracket UI immediately (instead of "TBD").
                if (totalRounds >= 2) {
                    final int bracketIndex = i;
                    final User byeUser = presentUser;
                    final int nextRound = 2;
                    final int nextPosition = bracketIndex / 2;
                    matchRepository
                            .findByTournamentIdAndRoundAndBracketPosition(
                                    tournament.getId(), nextRound, nextPosition)
                            .ifPresent(nextMatch -> {
                                if (bracketIndex % 2 == 0) {
                                    nextMatch.setPlayer1(byeUser);
                                } else {
                                    nextMatch.setPlayer2(byeUser);
                                }
                                if (nextMatch.getPlayer1() != null && nextMatch.getPlayer2() != null) {
                                    nextMatch.setInitialServer(nextMatch.getPlayer1().getUsername());
                                }
                                matchRepository.save(nextMatch);
                            });
                }
            } else {
                // Normal match
                User user1 = registrations.get(seed1 - 1).getUser();
                User user2 = registrations.get(seed2 - 1).getUser();

                Match match = new Match();
                match.setTournament(tournament);
                match.setRound(1);
                match.setBracketPosition(i);
                match.setPlayer1(user1);
                match.setPlayer2(user2);
                match.setInitialServer(user1.getUsername()); // first registered player serves first
                match.setStartDate(tournament.getStartDate());
                match.setStartTime("00:00");
                match.setVisibility(MatchVisibility.PUBLIC);
                match.setClub(tournament.getClub());
                matchRepository.save(match);
            }
        }

        // (round 2+ placeholders were already created above)

        // Update tournament status
        tournament.setStatus(TournamentStatus.DRAWN);
        tournamentRepository.save(tournament);

        return toDto(tournament, null);
    }

    public TournamentBracketDto getTournamentBracket(UUID tournamentId) {
        Tournament tournament = getTournamentEntity(tournamentId);
        List<Match> allMatches = matchRepository.findByTournamentIdOrderByRoundAscBracketPositionAsc(tournamentId);

        int totalRounds = 0;
        if (!allMatches.isEmpty()) {
            totalRounds = allMatches.stream()
                    .mapToInt(m -> m.getRound() != null ? m.getRound() : 0)
                    .max()
                    .orElse(0);
        }

        List<TournamentRoundDto> rounds = new ArrayList<>();
        for (int round = 1; round <= totalRounds; round++) {
            final int currentRound = round;
            List<TournamentMatchDto> roundMatches = allMatches.stream()
                    .filter(m -> m.getRound() != null && m.getRound() == currentRound)
                    .map(m -> {
                        String score = null;
                        boolean p1Winner = false;
                        boolean p2Winner = false;

                        if (m.isBye()) {
                            score = "W/O";
                            p1Winner = true;
                        } else if (m.isFinished()) {
                            score = m.getFinalScore();
                            if (score != null && !score.isBlank() && !score.equals("0 : 0")) {
                                String[] parts = score.split("\\s*:\\s*");
                                if (parts.length == 2) {
                                    try {
                                        int p1Sets = Integer.parseInt(parts[0]);
                                        int p2Sets = Integer.parseInt(parts[1]);
                                        p1Winner = p1Sets > p2Sets;
                                        p2Winner = p2Sets > p1Sets;
                                    } catch (NumberFormatException ignored) {
                                    }
                                }
                            }
                        }

                        return new TournamentMatchDto(
                                m.getId(),
                                m.getBracketPosition() != null ? m.getBracketPosition() : 0,
                                m.getPlayer1() != null ? m.getPlayer1().getFirstName() + " " + m.getPlayer1().getLastName() : null,
                                m.getPlayer1() != null ? m.getPlayer1().getUsername() : null,
                                m.getPlayer2() != null ? m.getPlayer2().getFirstName() + " " + m.getPlayer2().getLastName() : null,
                                m.getPlayer2() != null ? m.getPlayer2().getUsername() : null,
                                score,
                                m.isFinished(),
                                m.isBye(),
                                p1Winner,
                                p2Winner,
                                m.getLivePlayer1Games(),
                                m.getLivePlayer2Games(),
                                m.getLivePlayer1Points(),
                                m.getLivePlayer2Points()
                        );
                    })
                    .toList();

            String roundName;
            if (totalRounds == 1) {
                roundName = "Final";
            } else if (round == totalRounds) {
                roundName = "Final";
            } else if (round == totalRounds - 1) {
                roundName = "Semi-finals";
            } else if (round == totalRounds - 2) {
                roundName = "Quarter-finals";
            } else {
                roundName = "Round " + round;
            }

            rounds.add(new TournamentRoundDto(round, roundName, roundMatches));
        }

        return new TournamentBracketDto(
                tournament.getId(),
                tournament.getStatus().name(),
                totalRounds,
                rounds
        );
    }

    private int nextPowerOf2(int n) {
        int p = 1;
        while (p < n) p <<= 1;
        return p;
    }

    private int getSeedForBracketPosition(int position, int totalSlots) {
        if (position % 2 == 0) {
            return position / 2 + 1;
        } else {
            return totalSlots - position / 2;
        }
    }

    public TournamentDto toDto(Tournament tournament, String requesterUsername) {
        boolean isRegistered = false;
        if (requesterUsername != null) {
            User user = userRepository.findByUsername(requesterUsername).orElse(null);
            if (user != null) {
                isRegistered = registrationRepository.existsByTournamentAndUser(tournament, user);
            }
        }

        return new TournamentDto(
                tournament.getId(),
                tournament.getName(),
                tournament.getClub().getId(),
                tournament.getClub().getName(),
                tournament.getCreatedBy().getUsername(),
                tournament.getStartDate(),
                tournament.getRegistrationDeadline(),
                tournament.getStatus().name(),
                tournament.getMaxPlayers(),
                tournament.getNumberOfPlayers(),
                isRegistered,
                tournament.getCreatedAt()
        );
    }

    private Tournament getTournamentEntity(UUID tournamentId) {
        return tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.NOT_FOUND, "Tournament not found"));
    }

    @Transactional
    public void addReferee(String username, UUID tournamentId, String refereeUsername) {
        Tournament tournament = getTournamentEntity(tournamentId);

        if (!clubService.isClubAdmin(username, tournament.getClub().getId())) {
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "Only club admins can add referees");
        }

        User referee = userRepository.findByUsername(refereeUsername)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Referee user not found"));

        if (refereeRepository.existsByTournamentAndUser(tournament, referee)) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "User is already a referee for this tournament");
        }

        TournamentReferee tournamentReferee = new TournamentReferee();
        tournamentReferee.setTournament(tournament);
        tournamentReferee.setUser(referee);
        refereeRepository.save(tournamentReferee);
    }

    @Transactional
    public void removeReferee(String username, UUID tournamentId, String refereeUsername) {
        Tournament tournament = getTournamentEntity(tournamentId);

        if (!clubService.isClubAdmin(username, tournament.getClub().getId())) {
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "Only club admins can remove referees");
        }

        User referee = userRepository.findByUsername(refereeUsername)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Referee user not found"));

        TournamentReferee tournamentReferee = refereeRepository.findByTournamentAndUser(tournament, referee)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "User is not a referee for this tournament"));

        refereeRepository.delete(tournamentReferee);
    }

    public List<TournamentRefereeDto> getReferees(UUID tournamentId) {
        Tournament tournament = getTournamentEntity(tournamentId);
        return refereeRepository.findByTournament(tournament).stream()
                .map(r -> new TournamentRefereeDto(
                        r.getId(),
                        r.getUser().getUsername(),
                        r.getUser().getFirstName(),
                        r.getUser().getLastName()
                ))
                .toList();
    }

    @Transactional
    public void assignRefereeToMatch(String username, UUID tournamentId, UUID matchId, String refereeUsername) {
        Tournament tournament = getTournamentEntity(tournamentId);

        if (!clubService.isClubAdmin(username, tournament.getClub().getId())) {
            throw new ClientErrorException(HttpStatus.FORBIDDEN, "Only club admins can assign referees to matches");
        }

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Match not found"));

        if (!match.getTournament().getId().equals(tournamentId)) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Match does not belong to this tournament");
        }

        if (match.isFinished()) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Cannot assign referee to finished match");
        }

        User referee = userRepository.findByUsername(refereeUsername)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.BAD_REQUEST, "Referee user not found"));

        // Check if referee is registered for this tournament
        if (!refereeRepository.existsByTournamentAndUser(tournament, referee)) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "User is not a referee for this tournament");
        }

        // Check if referee is one of the players
        if (match.getPlayer1() != null && match.getPlayer1().getUsername().equals(refereeUsername)) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Referee cannot be a player in the match");
        }
        if (match.getPlayer2() != null && match.getPlayer2().getUsername().equals(refereeUsername)) {
            throw new ClientErrorException(HttpStatus.BAD_REQUEST, "Referee cannot be a player in the match");
        }

        match.setReferee(referee);
        matchRepository.save(match);
    }

    public TournamentStatusDto getTournamentStatus(UUID tournamentId) {
        Tournament tournament = getTournamentEntity(tournamentId);
        List<Match> allMatches = matchRepository.findByTournamentIdOrderByRoundAscBracketPositionAsc(tournamentId);

        int totalRounds = 0;
        if (!allMatches.isEmpty()) {
            totalRounds = allMatches.stream()
                    .mapToInt(m -> m.getRound() != null ? m.getRound() : 0)
                    .max()
                    .orElse(0);
        }

        int finishedMatches = (int) allMatches.stream().filter(Match::isFinished).count();
        int totalMatches = allMatches.size();

        int currentRound = 1;
        if (totalRounds > 0) {
            // Find the earliest round with unfinished matches
            for (int round = 1; round <= totalRounds; round++) {
                final int r = round;
                boolean hasUnfinished = allMatches.stream()
                        .filter(m -> m.getRound() != null && m.getRound() == r)
                        .anyMatch(m -> !m.isFinished());
                if (hasUnfinished) {
                    currentRound = round;
                    break;
                }
                if (round == totalRounds) {
                    currentRound = totalRounds;
                }
            }
        }

        return new TournamentStatusDto(
                tournament.getId(),
                tournament.getStatus().name(),
                totalRounds,
                currentRound,
                finishedMatches,
                totalMatches
        );
    }
}