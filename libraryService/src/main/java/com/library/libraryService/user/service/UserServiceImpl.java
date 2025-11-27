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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private EmailService emailService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long FAILED_WINDOW_MS = 10 * 60 * 1000L;
    private static final long LOCK_DURATION_MS  = 30 * 60 * 1000L;
    private static final long OTP_DURATION_MS   = 5 * 60 * 1000L;

    @Override
    @CacheEvict(value = "users", allEntries = true)
    public UserResponseDto registerUser(RegisterUserRequestDto request) {

        Optional<User> existingByUsername = userDao.findByUsername(request.getUsername());
        if (existingByUsername.isPresent()) {
            throw new RuntimeException("Username already taken");
        }

        Optional<User> existingByEmail = userDao.findByEmail(request.getEmail());
        if (existingByEmail.isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRole("VIEWER");
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());

        User saved = userDao.save(user);

        auditLogService.log("CREATE_USER","USER",saved.getId());

        return new UserResponseDto(
                saved.getId(),
                saved.getUsername(),
                saved.getEmail(),
                saved.getFullName(),
                saved.getRole(),
                saved.getCreatedAt(),
                saved.getUpdatedAt(),
                null
        );
    }

    @Override
    @Cacheable(value = "users", key = "'all_'+#sortType")
    public List<UserResponseDto> getAllUser(String sortType) {
        ArrayList<UserResponseDto> userResponseDto;

        userResponseDto = userDao.getAllUser().stream()
                .map(this::UserResponseDtoMapping).collect(Collectors.toCollection(ArrayList::new));

        bubbleSort(userResponseDto, sortType);

        auditLogService.log("GET_ALL_USER","USER","ALL");

        return userResponseDto;
    }

    @Override
    @CacheEvict(value = "users", allEntries = true)
    public void deleteById(String id) {
        User user = userDao.findById(id);
        if (user == null) {
            throw new AccessDeniedException("User tidak ditemukan");
        }

        auditLogService.log("DELETE_USER","USER",id);

        userDao.delete(user);
    }

    @Override
    @CacheEvict(value = "users", allEntries = true)
    public UserResponseDto updateUser(UpdateUserRequestDto request) {
        User user = userDao.findById(request.getId());
        if (user == null) {
            throw new AccessDeniedException("User tidak ditemukan");
        }

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRole(request.getRole());
        user.setUpdatedAt(new Date());

        User userSaved = userDao.save(user);

        auditLogService.log("UPDATE_USER","USER",userSaved.getId());

        return UserResponseDtoMapping(userSaved);
    }

    private UserResponseDto UserResponseDtoMapping(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLogin()
        );
    }

    private void bubbleSort(ArrayList<UserResponseDto> list, String sortType) {
        int size = list.size();
        boolean swapped;

        if (sortType.equals("asc")) {
            for (int i = 0; i < size - 1; i++) {
                swapped = false;
                for (int j = 0; j < size - 1 - i; j++) {
                    if (list.get(j).getCreatedAt().after(list.get(j + 1).getCreatedAt())) {
                        UserResponseDto temp = list.get(j);
                        list.set(j, list.get(j + 1));
                        list.set(j + 1, temp);
                        swapped = true;
                    }
                }
                if (!swapped) break;
            }
        }else if (sortType.equals("desc")) {
            for (int i = 0; i < size - 1; i++) {
                swapped = false;
                for (int j = 0; j < size - 1 - i; j++) {
                    if (list.get(j).getCreatedAt().before(list.get(j + 1).getCreatedAt())) {
                        UserResponseDto temp = list.get(j);
                        list.set(j, list.get(j + 1));
                        list.set(j + 1, temp);
                        swapped = true;
                    }
                }
                if (!swapped) break;
            }
        }else{
            throw new RuntimeException("Format Sorting Salah");
        }
    }

    @Override
    public LoginStep1ResponseDto loginStep1(LoginRequestDto request) {

        Optional<User> userOptional = userDao.findByUsernameEmail(request.getUsername());
        User user = userOptional.orElseThrow(() -> new InvalidLoginException("Username atau email tidak terdaftar"));

        ensureNotLocked(user);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            handleFailedLogin(user);
            auditLogService.log("LOGIN_FAILED", "AUTH", null, request.getUsername());
            throw new InvalidLoginException("password salah");
        }

        String otp = generateOtpCode();
        Date now = new Date();
        Date expiredAt = new Date(now.getTime() + OTP_DURATION_MS);

        user.setOtpCode(otp);
        user.setOtpExpiredAt(expiredAt);
        userDao.save(user);

        emailService.sendOtpEmail(user.getEmail(), otp);

        auditLogService.log("OTP_SENT", "AUTH", user.getId());

        return new LoginStep1ResponseDto("OTP Telah dikirim ke Email", user.getUsername()
        );
    }

    @Override
    @CacheEvict(value = "users", allEntries = true)
    public LoginResponseDto verifyOtp(VerifyOtpRequestDto request) {

        Optional<User> userOptional = userDao.findByUsernameEmail(request.getUsername());
        User user = userOptional.orElseThrow(() -> new InvalidLoginException("Username atau email tidak terdaftar"));

        ensureNotLocked(user);

        Date now = new Date();

        if (user.getOtpCode() == null || user.getOtpExpiredAt() == null || now.after(user.getOtpExpiredAt()) || !user.getOtpCode().equals(request.getOtp())) {

            handleFailedLogin(user);

            auditLogService.log("LOGIN_OTP_FAILED", "AUTH", user.getId());

            if (user.getOtpExpiredAt() != null && now.after(user.getOtpExpiredAt())){
                throw new InvalidOtpException("OTP TELAH KADALUARSA");
            }else{
                throw new InvalidOtpException("OTP SALAH ATAU BELUM ADA");
            }
        }

        resetFailedLogin(user);
        user.setOtpCode(null);
        user.setOtpExpiredAt(null);
        user.setLastLogin(new Date());
        userDao.save(user);

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole()
        );

        auditLogService.log("LOGIN_SUCCESS", "AUTH", user.getId());

        return new LoginResponseDto(
                user.getId(),
                user.getUsername(),
                "Login success",
                token
        );
    }

    private String generateOtpCode() {
        int code = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(code);
    }

    private void ensureNotLocked(User user) {
        Date now = new Date();
        if (user.getAccountLockedUntil() != null &&
                now.before(user.getAccountLockedUntil())) {

            long remainingMs = user.getAccountLockedUntil().getTime() - now.getTime();
            long remainingMinutes = remainingMs / 60000;
            throw new AccountLockedException(
                    "Akun terkunci. Coba lagi dalam " + remainingMinutes + " menit.");
        }
    }

    private void resetFailedLogin(User user) {
        user.setFailedLoginCount(0);
        user.setLastFailedLoginAt(null);
        user.setAccountLockedUntil(null);
        userDao.save(user);
    }

    private void handleFailedLogin(User user) {
        Date now = new Date();

        if (user.getLastFailedLoginAt() == null || now.getTime() - user.getLastFailedLoginAt().getTime() > FAILED_WINDOW_MS) {

            user.setFailedLoginCount(1);
            user.setLastFailedLoginAt(now);

        } else {
            int count = user.getFailedLoginCount() == null ? 0 : user.getFailedLoginCount();
            count++;
            user.setFailedLoginCount(count);
            user.setLastFailedLoginAt(now);

            if (count >= MAX_FAILED_ATTEMPTS) {
                Date lockedUntil = new Date(now.getTime() + LOCK_DURATION_MS);
                user.setAccountLockedUntil(lockedUntil);
                user.setFailedLoginCount(0);
            }
        }

        userDao.save(user);
    }
}
