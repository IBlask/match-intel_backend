package com.match_intel.backend.service;

import com.match_intel.backend.dto.request.RegisterUserRequest;
import com.match_intel.backend.dto.response.MatchDto;
import com.match_intel.backend.dto.response.UserDto;
import com.match_intel.backend.entity.FollowRequestStatus;
import com.match_intel.backend.entity.Match;
import com.match_intel.backend.entity.ProfileVisibility;
import com.match_intel.backend.entity.User;
import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.exception.GeneralUnhandledException;
import com.match_intel.backend.repository.FollowRequestRepository;
import com.match_intel.backend.repository.MatchRepository;
import com.match_intel.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private FollowRequestRepository followRequestRepository;
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private FollowService followService;


    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    public void saveUser(User user) {
        userRepository.save(user);
    }

    public void enableUser(User user) {
        user.setEnabled(true);
        userRepository.save(user);
    }


    public User registerUser(RegisterUserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ClientErrorException(HttpStatus.valueOf(400), "Entered email is already in use!");
        }
        else if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new ClientErrorException(HttpStatus.valueOf(400), "Entered username is taken! Try another.");
        }

        User newUser = new User(
                request.getUsername(),
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())
        );
        userRepository.save(newUser);

        return newUser;
    }


    public void changePassword(UUID userId, String newPassword) throws GeneralUnhandledException {
        if (newPassword == null || newPassword.isBlank()) {
            throw new GeneralUnhandledException("Please provide a password!");
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new GeneralUnhandledException("User not found!");
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }


    public List<User> searchUsers(String query) {
        if (query.isBlank()) {
            return userRepository.findTop50ByOrderByUsernameAsc();
        }
        String normalizedQuery = normalize(query);
        return userRepository.findAll().stream()
                .filter(user -> normalize(user.getFirstName()).contains(normalizedQuery)
                        || normalize(user.getLastName()).contains(normalizedQuery)
                        || normalize(user.getUsername()).contains(normalizedQuery))
                .limit(50)
                .toList();
    }

    private String normalize(String value) {
        return java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    public UserDto getUserByUsername(
            String currentUsername,
            String username
    ) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.NOT_FOUND, "User not found"));

        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.NOT_FOUND, "Current user not found"));

        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setUsername(user.getUsername());
        userDto.setProfileVisibility(user.getProfileVisibility().name());

        boolean isOwnProfile = currentUsername.equals(username);
        boolean restricted = false;

        if (!isOwnProfile) {
            switch (user.getProfileVisibility()) {
                case PUBLIC -> { }
                case FOLLOWERS -> {
                    restricted = !followService.isFollowing(currentUsername, username);
                }
                case PRIVATE -> restricted = true;
            }
        }

        userDto.setProfileRestricted(restricted);
        if (restricted) {
            return userDto;
        }

        userDto.setFollowing(followRequestRepository.findByFollowerAndStatus(user, FollowRequestStatus.ACCEPTED).size());
        userDto.setFollowers(followRequestRepository.findByFolloweeAndStatus(user, FollowRequestStatus.ACCEPTED).size());
        userDto.setDoesFollow(followRequestRepository.existsByFollowerAndFolloweeAndStatus(currentUser, user, FollowRequestStatus.ACCEPTED));
        userDto.setFollowRequestSent(followRequestRepository.existsByFollowerAndFolloweeAndStatus(currentUser, user, FollowRequestStatus.PENDING));

        if (isOwnProfile) {
            userDto.setMatches(toMatchDtos(matchRepository.findByPlayer1OrPlayer2(user, user)));
        }
        else {
            userDto.setMatches(toMatchDtos(matchRepository.findVisibleMatchesOfFollowees(currentUser.getId())));
        }

        return userDto;
    }

    private List<MatchDto> toMatchDtos(List<Match> matches) {
        if (matches == null) return Collections.emptyList();
        List<MatchDto> result = new ArrayList<>();
        for (Match m : matches) {
            result.add(toMatchDto(m));
        }
        return result;
    }

    private MatchDto toMatchDto(Match m) {
        MatchDto dto = new MatchDto();
        dto.setId(m.getId());
        dto.setInitialServer(m.getInitialServer());
        dto.setStartDate(m.getStartDate());
        dto.setStartTime(m.getStartTime());
        dto.setFinalScore(m.getFinalScore());
        dto.setVisibility(m.getVisibility() != null ? m.getVisibility().name() : null);
        dto.setFinished(m.isFinished());
        dto.setPlayer1Efficiency(m.getPlayer1Efficiency());
        dto.setPlayer2Efficiency(m.getPlayer2Efficiency());
        dto.setNumberOfLikes(m.getNumberOfLikes());
        dto.setNumberOfComments(m.getNumberOfComments());
        if (m.getPlayer1() != null) {
            dto.setPlayer1(new com.match_intel.backend.dto.response.PlayerInfoDto(
                    m.getPlayer1().getId() != null ? m.getPlayer1().getId().toString() : null,
                    m.getPlayer1().getUsername(),
                    m.getPlayer1().getFirstName(),
                    m.getPlayer1().getLastName()
            ));
        }
        if (m.getPlayer2() != null) {
            dto.setPlayer2(new com.match_intel.backend.dto.response.PlayerInfoDto(
                    m.getPlayer2().getId() != null ? m.getPlayer2().getId().toString() : null,
                    m.getPlayer2().getUsername(),
                    m.getPlayer2().getFirstName(),
                    m.getPlayer2().getLastName()
            ));
        }
        if (m.getReferee() != null) {
            dto.setReferee(new com.match_intel.backend.dto.response.PlayerInfoDto(
                    m.getReferee().getId() != null ? m.getReferee().getId().toString() : null,
                    m.getReferee().getUsername(),
                    m.getReferee().getFirstName(),
                    m.getReferee().getLastName()
            ));
        }
        if (m.getClub() != null) {
            dto.setClubName(m.getClub().getName());
        }
        dto.setLikedByUser(m.isLikedByUser());
        return dto;
    }

    public void updateProfileVisibility(String username, ProfileVisibility visibility) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.NOT_FOUND, "User not found"));
        user.setProfileVisibility(visibility);
        userRepository.save(user);
    }
}