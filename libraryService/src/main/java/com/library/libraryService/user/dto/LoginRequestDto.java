package com.library.libraryService.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequestDto {

    @NotBlank(message = "username tidak boleh kosong")
    @NotNull(message = "username wajib diisi")
    private String username;

    @NotBlank(message = "password tidak boleh kosong")
    @NotNull(message = "password wajib diisi")
    private String password;
}

