package com.library.libraryService.user.service;

import com.library.libraryService.user.dto.*;

import java.util.List;

public interface UserService {

    UserResponseDto registerUser(RegisterUserRequestDto request);
    List<UserResponseDto> getAllUser(String sortType);
    UserResponseDto updateUser(UpdateUserRequestDto request);
    void deleteById(String id);
    LoginStep1ResponseDto loginStep1(LoginRequestDto request);
    LoginResponseDto verifyOtp(VerifyOtpRequestDto request);
}