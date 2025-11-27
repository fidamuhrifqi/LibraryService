package com.library.libraryService.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {

    private String id;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private Date createdAt;
    private Date updatedAt;
    private Date lastLogin;
}