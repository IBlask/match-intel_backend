package com.match_intel.backend.service;

import com.match_intel.backend.dto.request.RegisterUserRequest;
import com.match_intel.backend.dto.response.UserDto;
import com.match_intel.backend.entity.FollowRequestStatus;
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

import java.util.List;
import java.util.Optional;
import java.util.Locale;
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
            userDto.setMatches(matchRepository.findByPlayer1OrPlayer2(user, user));
        }
        else {
            userDto.setMatches(matchRepository.findVisibleMatchesOfFollowees(currentUser.getId()));
        }

        return userDto;
    }

    public void updateProfileVisibility(String username, ProfileVisibility visibility) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ClientErrorException(HttpStatus.NOT_FOUND, "User not found"));
        user.setProfileVisibility(visibility);
        userRepository.save(user);
    }
}