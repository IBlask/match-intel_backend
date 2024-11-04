package com.match_intel.backend.entity;

import jakarta.persistence.*;

import java.util.UUID;

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
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private boolean isEmailConfirmed;
    @Column
    private Integer emailVerificationCode;
    @Column
    private String profileImage;


    public User() {}

    public User(String username, String firstName, String lastName, String email, String password) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.isEmailConfirmed = false;
        this.emailVerificationCode = 0;
    }
}