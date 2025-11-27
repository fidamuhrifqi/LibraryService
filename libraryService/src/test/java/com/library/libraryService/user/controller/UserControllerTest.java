package com.library.libraryService.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.libraryService.security.jwt.JwtUtil;
import com.library.libraryService.security.ratelimit.RateLimiterService;
import com.library.libraryService.user.dto.*;
import com.library.libraryService.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private RateLimiterService rateLimiterService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /users/register sukses register user")
    void register_success() throws Exception {
        RegisterUserRequestDto requestDto = new RegisterUserRequestDto();
         requestDto.setUsername("fida");
         requestDto.setEmail("fida@testmail.com");
         requestDto.setPassword("P@ss0wrd");
        requestDto.setFullName("fidaaaa");

        UserResponseDto responseDto = new UserResponseDto();
         responseDto.setId("123123");
         responseDto.setUsername("fida");

        Mockito.when(userService.registerUser(any(RegisterUserRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(
                        post("/users/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123123"));
    }

    @Test
    @DisplayName("POST /users/login sukses login step 1")
    void loginStep1_success() throws Exception {
        LoginRequestDto requestDto = new LoginRequestDto();
         requestDto.setUsername("fida");
         requestDto.setPassword("P@ssw0rd");

        LoginStep1ResponseDto responseDto = new LoginStep1ResponseDto();
         responseDto.setMessage("OTP sent");

        Mockito.when(userService.loginStep1(any(LoginRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(
                        post("/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto))
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /users/verify-otp sukses login step 2")
    void verifyOtp_success() throws Exception {
        VerifyOtpRequestDto requestDto = new VerifyOtpRequestDto();
         requestDto.setUsername("fida");
         requestDto.setOtp("123123");

        LoginResponseDto responseDto = new LoginResponseDto();
         responseDto.setToken("asdadasdad");

        Mockito.when(userService.verifyOtp(any(VerifyOtpRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(
                        post("/users/verify-otp")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("asdadasdad"));
    }

    @Test
    @DisplayName("GET /users/getAll/{sortType} sukses get semua user")
    void getAllUsers_success() throws Exception {
        UserResponseDto user1 = new UserResponseDto();
        UserResponseDto user2 = new UserResponseDto();
         user1.setId("1");
         user2.setId("2");

        List<UserResponseDto> responseList = List.of(user1, user2);

        Mockito.when(userService.getAllUser("asc"))
                .thenReturn(responseList);

        mockMvc.perform(
                        get("/users/getAll/{sortType}", "asc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /users/getAll/{sortType} validasi sortType kosong error 400")
    void getAllUsers_validationError_blankSortType() throws Exception {
        mockMvc.perform(
                        get("/users/getAll/{sortType}", " ")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /users/update sukses update user")
    void updateUser_success() throws Exception {
        UpdateUserRequestDto requestDto = new UpdateUserRequestDto();
        requestDto.setId("123123123");
        requestDto.setRole("VIEWER");
        requestDto.setEmail("fida@test.com");
        requestDto.setPassword("P@ssw0rd");
        requestDto.setUsername("fida");
        requestDto.setFullName("fidaaaa");

        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId("123123123");
        responseDto.setRole("VIEWER");

        Mockito.when(userService.updateUser(any(UpdateUserRequestDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(
                        post("/users/update")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto))
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /users/delete/{id} sukses delete user")
    void deleteUser_success() throws Exception {
        mockMvc.perform(
                        delete("/users/delete/{id}", "123123")
                ).andExpect(status().isNoContent());
    }
}
