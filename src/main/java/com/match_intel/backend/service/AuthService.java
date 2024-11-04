package com.match_intel.backend.service;

import com.match_intel.backend.dto.request.RegisterUserRequest;
import com.match_intel.backend.entity.User;
import com.match_intel.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserRepository userRepository;


    public Optional<String> register(RegisterUserRequest registerUserRequest) {
        if (userRepository.findByEmail(registerUserRequest.getEmail()).isPresent()) {
            return Optional.of("Entered email is already in use!");
        }
        else if (userRepository.findByUsername(registerUserRequest.getUsername()).isPresent()) {
            return Optional.of("Entered username is taken! Try another.");
        }

        User newUser = new User(
                registerUserRequest.getUsername(),
                registerUserRequest.getFirstName(),
                registerUserRequest.getLastName(),
                registerUserRequest.getEmail(),
                passwordEncoder.encode(registerUserRequest.getPassword())
        );
        userRepository.save(newUser);

        return Optional.empty();
    }
}
