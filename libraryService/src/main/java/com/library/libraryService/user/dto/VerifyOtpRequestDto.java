package com.library.libraryService.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VerifyOtpRequestDto {

    @NotBlank(message = "username tidak boleh kosong")
    @NotNull(message = "username wajib diisi")
    private String username;

    @NotBlank(message = "otp tidak boleh kosong")
    @NotNull(message = "otp wajib diisi")
    private String otp;
}
