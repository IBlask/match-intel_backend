package com.match_intel.backend.service;

import com.match_intel.backend.dto.request.RegisterUserRequest;
import com.match_intel.backend.entity.User;
import com.match_intel.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;


    public Pair<Optional<User>, Optional<String>> registerUser(RegisterUserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return Pair.of(Optional.empty(), Optional.of("Entered email is already in use!"));
        }
        else if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return Pair.of(Optional.empty(), Optional.of("Entered username is taken! Try another."));
        }

        User newUser = new User(
                request.getUsername(),
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())
        );
        userRepository.save(newUser);

        return Pair.of(Optional.of(newUser), Optional.empty());
    }
}