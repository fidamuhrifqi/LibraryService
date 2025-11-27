package com.library.libraryService.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterUserRequestDto {

    @NotBlank(message = "username tidak boleh kosong")
    @NotNull(message = "username wajib diisi")
    private String username;

    @NotBlank(message = "email tidak boleh kosong")
    @NotNull(message = "email wajib diisi")
    @Email(message = "Format email tidak valid")
    private String email;

    @NotBlank(message = "password tidak boleh kosong")
    @NotNull(message = "password wajib diisi")
    private String password;

    @NotBlank(message = "fullName tidak boleh kosong")
    @NotNull(message = "fullName wajib diisi")
    private String fullName;
}