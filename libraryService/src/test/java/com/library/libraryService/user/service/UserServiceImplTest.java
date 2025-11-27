package com.library.libraryService.user.service;

import com.library.libraryService.audit.service.AuditLogService;
import com.library.libraryService.common.mail.EmailService;
import com.library.libraryService.security.jwt.JwtUtil;
import com.library.libraryService.user.dao.UserDao;
import com.library.libraryService.user.dto.*;
import com.library.libraryService.user.entity.User;
import com.library.libraryService.user.exception.AccountLockedException;
import com.library.libraryService.user.exception.InvalidLoginException;
import com.library.libraryService.user.exception.InvalidOtpException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserDao userDao;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserServiceImpl userService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    @DisplayName("registerUser sukses register user baru")
    void registerUser_success() {
        RegisterUserRequestDto request = new RegisterUserRequestDto();
        request.setUsername("fida");
        request.setEmail("fida@test.com");
        request.setPassword("P@ssw0rd");
        request.setFullName("Fidaaaaa");

        when(userDao.findByUsername("fida")).thenReturn(Optional.empty());
        when(userDao.findByEmail("fida@test.com")).thenReturn(Optional.empty());

        when(userDao.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId("123123");
            return u;
        });

        UserResponseDto result = userService.registerUser(request);

        assertThat(result.getId()).isEqualTo("123123");

        verify(userDao).findByUsername("fida");
        verify(userDao).findByEmail("fida@test.com");
        verify(userDao).save(any(User.class));
        verify(auditLogService).log("CREATE_USER", "USER", "123123");
    }

    @Test
    @DisplayName("registerUser gagal jika username sudah dipakai")
    void registerUser_usernameAlreadyTaken() {
        RegisterUserRequestDto request = new RegisterUserRequestDto();
        request.setUsername("fida");
        request.setEmail("fida@test.com");

        when(userDao.findByUsername("fida")).thenReturn(Optional.of(new User()));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.registerUser(request));

        assertThat(ex.getMessage()).isEqualTo("Username already taken");
        verify(userDao, never()).save(any());
    }

    @Test
    @DisplayName("registerUser gagal jika email sudah terdaftar")
    void registerUser_emailAlreadyRegistered() {
        RegisterUserRequestDto request = new RegisterUserRequestDto();
        request.setUsername("fida");
        request.setEmail("fida@test.com");

        when(userDao.findByUsername("fida")).thenReturn(Optional.empty());
        when(userDao.findByEmail("fida@test.com")).thenReturn(Optional.of(new User()));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.registerUser(request));

        assertThat(ex.getMessage()).isEqualTo("Email already registered");
        verify(userDao, never()).save(any());
    }

    @Test
    @DisplayName("getAllUser  mengembalikan list yang sudah di bubble sort ASC")
    void getAllUser_sortedAsc() {
        User u1 = new User();
        u1.setId("123123");
        u1.setUsername("fida");
        Date older = new Date(System.currentTimeMillis() - 10_000);
        u1.setCreatedAt(older);

        User u2 = new User();
        u2.setId("234234");
        u2.setUsername("udin");
        Date newer = new Date(System.currentTimeMillis());
        u2.setCreatedAt(newer);

        when(userDao.getAllUser()).thenReturn(Arrays.asList(u2, u1));

        List<UserResponseDto> result = userService.getAllUser("asc");

        assertThat(result).hasSize(2);

        assertThat(result.get(0).getId()).isEqualTo("123123");
        assertThat(result.get(1).getId()).isEqualTo("234234");

        verify(auditLogService).log("GET_ALL_USER", "USER", "ALL");
    }

    @Test
    @DisplayName("getAllUser mengembalikan list yang sudah di bubble sort Desc")
    void getAllUser_sortedDesc() {
        User u1 = new User();
        u1.setId("123123");
        u1.setUsername("fida");
        Date older = new Date(System.currentTimeMillis() + 10_000);
        u1.setCreatedAt(older);

        User u2 = new User();
        u2.setId("234234");
        u2.setUsername("udin");
        Date newer = new Date(System.currentTimeMillis());
        u2.setCreatedAt(newer);

        when(userDao.getAllUser()).thenReturn(Arrays.asList(u2, u1));

        List<UserResponseDto> result = userService.getAllUser("desc");

        assertThat(result).hasSize(2);

        assertThat(result.get(0).getId()).isEqualTo("123123");
        assertThat(result.get(1).getId()).isEqualTo("234234");

        verify(auditLogService).log("GET_ALL_USER", "USER", "ALL");
    }

    @Test
    @DisplayName("deleteById sukses ketika user ditemukan")
    void deleteById_success() {
        String id = "123123";
        User user = new User();
        user.setId(id);

        when(userDao.findById(id)).thenReturn(user);

        userService.deleteById(id);

        verify(auditLogService).log("DELETE_USER", "USER", id);
        verify(userDao).delete(user);
    }

    @Test
    @DisplayName("deleteById throw AccessDeniedException jika user tidak ditemukan")
    void deleteById_userNotFound() {
        when(userDao.findById("123123")).thenReturn(null);

        assertThrows(AccessDeniedException.class,
                () -> userService.deleteById("123123"));

        verify(userDao, never()).delete(any());
    }

    @Test
    @DisplayName("updateUser sukses update data user")
    void updateUser_success() {
        UpdateUserRequestDto request = new UpdateUserRequestDto();
        request.setId("123123");
        request.setUsername("fida");
        request.setEmail("fida@test.com");
        request.setPassword("P@ssw0rd");
        request.setFullName("Fidaaaa");
        request.setRole("ADMIN");

        User existing = new User();
        existing.setId("123123");
        existing.setUsername("fida");
        existing.setEmail("fida@test.com");
        existing.setPassword(passwordEncoder.encode("P@ssw0rd"));
        existing.setFullName("Fidaaaaaaaaaaaaaaaaaaa");
        existing.setRole("VIEWER");
        existing.setCreatedAt(new Date());

        when(userDao.findById("123123")).thenReturn(existing);
        when(userDao.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDto result = userService.updateUser(request);

        assertThat(result.getId()).isEqualTo("123123");
        assertThat(result.getUsername()).isEqualTo("fida");
        assertThat(result.getEmail()).isEqualTo("fida@test.com");
        assertThat(result.getRole()).isEqualTo("ADMIN");

        verify(auditLogService).log("UPDATE_USER", "USER", "123123");
    }

    @Test
    @DisplayName("updateArticle throw AccessDeniedException jika user tidak ditemukan")
    void updateArticle_userNotFound() {
        UpdateUserRequestDto request = new UpdateUserRequestDto();
        request.setId("123123");

        when(userDao.findById("123123")).thenReturn(null);

        assertThrows(AccessDeniedException.class,
                () -> userService.updateUser(request));

        verify(userDao, never()).save(any());
    }

    @Test
    @DisplayName("loginStep1 sukses kalau username & password benar, kirim OTP")
    void loginStep1_success() {
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("fida");
        request.setPassword("P@ssw0rd");

        User user = new User();
        user.setId("123123");
        user.setUsername("fida");
        user.setEmail("fida@test.com");
        user.setPassword(passwordEncoder.encode("P@ssw0rd"));

        when(userDao.findByUsernameEmail("fida")).thenReturn(Optional.of(user));
        when(userDao.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoginStep1ResponseDto response = userService.loginStep1(request);

        assertThat(response.getMessage()).isEqualTo("OTP Telah dikirim ke Email");
        assertThat(response.getUsername()).isEqualTo("fida");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDao, atLeastOnce()).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getOtpCode()).isNotNull();
        assertThat(savedUser.getOtpExpiredAt()).isNotNull();

        verify(emailService).sendOtpEmail(eq("fida@test.com"), anyString());
        verify(auditLogService).log("OTP_SENT", "AUTH", "123123");
    }

    @Test
    @DisplayName("loginStep1 throw InvalidLoginException kalau username/email tidak terdaftar")
    void loginStep1_userNotFound() {
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("asdasdadas");
        request.setPassword("qweqweqwe");

        when(userDao.findByUsernameEmail("asdasdadas")).thenReturn(Optional.empty());

        InvalidLoginException ex = assertThrows(InvalidLoginException.class,
                () -> userService.loginStep1(request));

        assertThat(ex.getMessage()).isEqualTo("Username atau email tidak terdaftar");
    }

    @Test
    @DisplayName("loginStep1 throw InvalidLoginException kalau password salah")
    void loginStep1_wrongPassword() {
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("fida");
        request.setPassword("password");

        User user = new User();
        user.setId("123123");
        user.setUsername("fida");
        user.setEmail("fida@test.com");
        user.setLastFailedLoginAt(new Date(System.currentTimeMillis() - 10_000));
        user.setFailedLoginCount(3);
        user.setPassword(passwordEncoder.encode("P@ssw0rd"));

        when(userDao.findByUsernameEmail("fida")).thenReturn(Optional.of(user));
        when(userDao.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InvalidLoginException ex = assertThrows(InvalidLoginException.class,
                () -> userService.loginStep1(request));

        assertThat(ex.getMessage()).isEqualTo("password salah");
        verify(auditLogService).log(eq("LOGIN_FAILED"), eq("AUTH"), isNull(), eq("fida"));
        verify(userDao, atLeastOnce()).save(any(User.class));
    }

    @Test
    @DisplayName("loginStep1 throw AccountLockedException akun terkunci saat percobaan ke 5")
    void loginStep1_accountLockedAfter5try() {
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("fida");
        request.setPassword("password");

        User user = new User();
        user.setId("123123");
        user.setUsername("fida");
        user.setPassword(passwordEncoder.encode("P@ssw0rd"));
        user.setLastFailedLoginAt(new Date(System.currentTimeMillis() - 10_000));
        user.setFailedLoginCount(5);

        when(userDao.findByUsernameEmail("fida")).thenReturn(Optional.of(user));

        assertThrows(InvalidLoginException.class,
                () -> userService.loginStep1(request));
    }

    @Test
    @DisplayName("loginStep1 throw AccountLockedException kalau akun masih terkunci")
    void loginStep1_accountLocked() {
        LoginRequestDto request = new LoginRequestDto();
        request.setUsername("fida");
        request.setPassword("password");

        User user = new User();
        user.setId("123123");
        user.setUsername("fida");
        user.setPassword(passwordEncoder.encode("P@ssw0rd"));
        user.setAccountLockedUntil(new Date(System.currentTimeMillis() + 60_000));

        when(userDao.findByUsernameEmail("fida")).thenReturn(Optional.of(user));

        assertThrows(AccountLockedException.class,
                () -> userService.loginStep1(request));
    }

    @Test
    @DisplayName("verifyOtp sukses kalau OTP benar dan belum kadaluarsa")
    void verifyOtp_success() {
        VerifyOtpRequestDto request = new VerifyOtpRequestDto();
        request.setUsername("fida");
        request.setOtp("123456");

        User user = new User();
        user.setId("123123");
        user.setUsername("fida");
        user.setRole("VIEWER");
        user.setOtpCode("123456");
        user.setOtpExpiredAt(new Date(System.currentTimeMillis() + 60_000)); // belum kadaluarsa

        when(userDao.findByUsernameEmail("fida")).thenReturn(Optional.of(user));
        when(userDao.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtil.generateToken("123123", "fida", "VIEWER")).thenReturn("asdasdasd");

        LoginResponseDto response = userService.verifyOtp(request);

        assertThat(response.getUserId()).isEqualTo("123123");
        assertThat(response.getUsername()).isEqualTo("fida");
        assertThat(response.getMessage()).isEqualTo("Login success");
        assertThat(response.getToken()).isEqualTo("asdasdasd");

        verify(jwtUtil).generateToken("123123", "fida", "VIEWER");
        verify(auditLogService).log("LOGIN_SUCCESS", "AUTH", "123123");
    }

    @Test
    @DisplayName("verifyOtp throw InvalidLoginException kalau username/email tidak terdaftar")
    void verifyOtp_userNotFound() {
        VerifyOtpRequestDto request = new VerifyOtpRequestDto();
        request.setUsername("qweqweqwe");
        request.setOtp("123456");

        when(userDao.findByUsernameEmail("qweqweqwe")).thenReturn(Optional.empty());

        InvalidLoginException ex = assertThrows(InvalidLoginException.class,
                () -> userService.verifyOtp(request));

        assertThat(ex.getMessage()).isEqualTo("Username atau email tidak terdaftar");
    }

    @Test
    @DisplayName("verifyOtp throw InvalidOtpException kalau OTP salah")
    void verifyOtp_wrongOtp() {
        VerifyOtpRequestDto request = new VerifyOtpRequestDto();
        request.setUsername("fida");
        request.setOtp("000000");

        User user = new User();
        user.setId("123123");
        user.setUsername("fida");
        user.setOtpCode("123456");
        user.setOtpExpiredAt(new Date(System.currentTimeMillis() + 60_000));

        when(userDao.findByUsernameEmail("fida")).thenReturn(Optional.of(user));
        when(userDao.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InvalidOtpException ex = assertThrows(InvalidOtpException.class,
                () -> userService.verifyOtp(request));

        assertThat(ex.getMessage()).isEqualTo("OTP SALAH ATAU BELUM ADA");
        verify(auditLogService).log("LOGIN_OTP_FAILED", "AUTH", "123123");
    }

    @Test
    @DisplayName("verifyOtp throw InvalidOtpException kalau OTP kadaluarsa")
    void verifyOtp_expiredOtp() {
        VerifyOtpRequestDto request = new VerifyOtpRequestDto();
        request.setUsername("fida");
        request.setOtp("123456");

        User user = new User();
        user.setId("123123");
        user.setUsername("fida");
        user.setOtpCode("123456");
        user.setOtpExpiredAt(new Date(System.currentTimeMillis() - 60_000)); // sudah lewat

        when(userDao.findByUsernameEmail("fida")).thenReturn(Optional.of(user));
        when(userDao.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InvalidOtpException ex = assertThrows(InvalidOtpException.class,
                () -> userService.verifyOtp(request));

        assertThat(ex.getMessage()).isEqualTo("OTP TELAH KADALUARSA");
        verify(auditLogService).log("LOGIN_OTP_FAILED", "AUTH", "123123");
    }

    @Test
    @DisplayName("verifyOtp throw AccountLockedException kalau akun terkunci")
    void verifyOtp_accountLocked() {
        VerifyOtpRequestDto request = new VerifyOtpRequestDto();
        request.setUsername("fida");
        request.setOtp("123456");

        User user = new User();
        user.setId("123123");
        user.setUsername("fida");
        user.setAccountLockedUntil(new Date(System.currentTimeMillis() + 60_000));

        when(userDao.findByUsernameEmail("fida")).thenReturn(Optional.of(user));

        assertThrows(AccountLockedException.class,
                () -> userService.verifyOtp(request));
    }
}