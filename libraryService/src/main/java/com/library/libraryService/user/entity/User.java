package com.library.libraryService.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    private String fullName;
    private String role;
    private Date createdAt;
    private Date updatedAt;
    private Date lastLogin;

    private String otpCode;
    private Date otpExpiredAt;

    private Integer failedLoginCount;
    private Date lastFailedLoginAt;
    private Date accountLockedUntil;
}
