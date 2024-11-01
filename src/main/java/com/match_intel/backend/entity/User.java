package com.match_intel.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "Users")
public class User {

    @Id
    @GeneratedValue(generator = "GenerationType.UUID")
    private UUID id;
    private String name;
    private String surname;
    private String email;
    private boolean isEmailConfirmed;
    private Integer emailVerificationCode;
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
