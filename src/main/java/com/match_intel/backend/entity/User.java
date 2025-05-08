package com.match_intel.backend.entity;

import com.match_intel.backend.exception.GeneralUnhandledException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Entity
@Table(name = "Users")
public class User {

    @Id
    @GeneratedValue(generator = "GenerationType.UUID")
    private UUID id;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    @Setter
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    @Setter
    @Column(nullable = false)
    private boolean enabled;
    @Column
    private String profileImage;


    public User() {}

    public User(String username, String firstName, String lastName, String email, String password) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.enabled = false;
    }


    public void setPassword(String password) throws GeneralUnhandledException {
        if (password == null || password.isBlank()) {
            throw new GeneralUnhandledException("Please provide a password!");
        }
        this.password = password;
    }
}