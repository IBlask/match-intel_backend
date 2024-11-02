package com.match_intel.backend.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "Users")
public class User {

    @Id
    @GeneratedValue(generator = "GenerationType.UUID")
    private UUID id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String surname;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private boolean isEmailConfirmed;
    @Column
    private Integer emailVerificationCode;
    @Column
    private String profileImage;


    public User(String name, String surname, String email, Integer emailVerificationCode, String profileImage) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.isEmailConfirmed = false;
        this.emailVerificationCode = emailVerificationCode;
        this.profileImage = profileImage;
    }

}
