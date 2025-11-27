package com.library.libraryService.user.controller;

import com.library.libraryService.user.dto.*;
import com.library.libraryService.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/users")
@SecurityRequirement(name = "bearer-jwt")
public class UserController {

    @Autowired
    private UserService userService;

    @Tag(name = "User", description = "User Management")
    @Operation(summary = "Register new user")
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody RegisterUserRequestDto request) {
        UserResponseDto response = userService.registerUser(request);
        return ResponseEntity.ok(response);
    }

    @Tag(name = "Auth", description = "User Login & MFA")
    @Operation(summary = "Step 1 login using username/email and password then send otp to email")
    @PostMapping("/login")
    public ResponseEntity<LoginStep1ResponseDto> loginStep1(@Valid @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(userService.loginStep1(request));
    }

    @Tag(name = "Auth", description = "User Login & MFA")
    @Operation(summary = "Step 2 login using username/email and OTP and get JWT")
    @PostMapping("/verify-otp")
    public ResponseEntity<LoginResponseDto> verifyOtp(@Valid @RequestBody VerifyOtpRequestDto request) {
        return ResponseEntity.ok(userService.verifyOtp(request));
    }

    @Tag(name = "User", description = "User Management")
    @Operation(summary = "Get all data user then sorted using bubble sort algorithm")
    @GetMapping("/getAll/{sortType}")
    public ResponseEntity<List<UserResponseDto>> getAllArticles(@PathVariable @NotNull(message = "sortType wajib diisi") @NotBlank(message = "sortType tidak boleh kosong") String sortType) {
        return ResponseEntity.ok(userService.getAllUser(sortType));
    }

    @Tag(name = "User", description = "User Management")
    @Operation(summary = "Update user data by super admin")
    @PostMapping("/update")
    public ResponseEntity<UserResponseDto> updateArticle(@Valid @RequestBody UpdateUserRequestDto request) {
        return ResponseEntity.ok(userService.updateUser(request));
    }

    @Tag(name = "User", description = "User Management")
    @Operation(summary = "Delete user data by super admin")
    @DeleteMapping("delete/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable @NotNull(message = "ID wajib diisi") @NotBlank(message = "ID tidak boleh kosong") String id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}