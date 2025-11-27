package com.library.libraryService.config;

import com.library.libraryService.user.dao.UserDao;
import com.library.libraryService.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UserDao userDao;

    @Mock
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        dataInitializer = new DataInitializer(userDao);
    }

    @Test
    @DisplayName("tidak run ketika initSuperAdmin False")
    void run_shouldDoNothing_whenInitSuperAdminIsFalse() throws Exception {
        ReflectionTestUtils.setField(dataInitializer, "initSuperAdmin", false);

        dataInitializer.run();

        verifyNoInteractions(userDao);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("run tidak melakukan apa apa ketika table user ada isinya")
    void run_shouldDoNothing_whenUserAlreadyExist() throws Exception {
        ReflectionTestUtils.setField(dataInitializer, "initSuperAdmin", true);
        when(userDao.count()).thenReturn(3L);

        dataInitializer.run();

        verify(userDao).count();
        verifyNoMoreInteractions(userDao);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("run insert super admin ketika table user kosong")
    void run_shouldCreateDefaultSuperAdmin_whenNoUserExist() throws Exception {
        ReflectionTestUtils.setField(dataInitializer, "initSuperAdmin", true);
        when(userDao.count()).thenReturn(0L);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        dataInitializer.run();

        verify(userDao).count();
        verify(userDao).save(userCaptor.capture());

        User saved = userCaptor.getValue();
        assertThat(saved.getUsername()).isEqualTo("fida");
        assertThat(saved.getUpdatedAt()).isNotNull();
    }
}
