package com.match_intel.backend.service;

import com.match_intel.backend.dto.request.RegisterUserRequest;
import com.match_intel.backend.entity.User;
import com.match_intel.backend.exception.ClientErrorException;
import com.match_intel.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;


    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    public void saveUser(User user) {
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
}