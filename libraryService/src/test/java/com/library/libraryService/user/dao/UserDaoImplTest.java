package com.library.libraryService.user.dao;

import com.library.libraryService.user.entity.User;
import com.library.libraryService.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
public class UserDaoImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDaoImpl userDao;

    @Test
    @DisplayName("save() sukses get dao save")
    void save_shouldCallRepositorySave() {
        User user = new User();
        Mockito.when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userDao.save(user);

        assertThat(result).isSameAs(user);
        Mockito.verify(userRepository).save(eq(user));
    }

    @Test
    @DisplayName("findAll() sukses get dao findAll")
    void findAll_shouldReturnListFromRepository() {
        User a1 = new User();
        User a2 = new User();
        List<User> list = List.of(a1, a2);

        Mockito.when(userRepository.findAll()).thenReturn(list);

        List<User> result = userDao.getAllUser();

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(a1, a2);
        Mockito.verify(userRepository).findAll();
    }

    @Test
    @DisplayName("findById() sukses get dao findById")
    void findById_shouldReturnUser_whenPresent() {
        String id = "123123";
        User User = new User();

        Mockito.when(userRepository.findById(id)).thenReturn(Optional.of(User));

        User result = userDao.findById(id);

        assertThat(result).isSameAs(User);
        Mockito.verify(userRepository).findById(id);
    }

    @Test
    @DisplayName("findById() return null")
    void findById_shouldReturnNull_whenNotPresent() {
        String id = "1231231";

        Mockito.when(userRepository.findById(id)).thenReturn(Optional.empty());

        User result = userDao.findById(id);

        assertThat(result).isNull();
        Mockito.verify(userRepository).findById(id);
    }

    @Test
    @DisplayName("delete() sukses get dao delete")
    void delete_shouldCallRepositoryDelete() {
        User User = new User();

        userDao.delete(User);

        Mockito.verify(userRepository).delete(User);
    }

    @Test
    @DisplayName("findByUserName() sukses get dao findByUserName")
    void findByUserName_shouldReturnUser_whenPresent() {
        String userName = "123123";
        User User = new User();

        Mockito.when(userRepository.findByUsername(userName)).thenReturn(Optional.of(User));

        Optional<User> result = userDao.findByUsername(userName);

        assertThat(result.orElse(null)).isSameAs(User);
        Mockito.verify(userRepository).findByUsername(userName);
    }

    @Test
    @DisplayName("findByEmail() sukses get dao findByEmail")
    void findByEmail_shouldReturnUser_whenPresent() {
        String email = "123123";
        User User = new User();

        Mockito.when(userRepository.findByEmail(email)).thenReturn(Optional.of(User));

        Optional<User> result = userDao.findByEmail(email);

        assertThat(result.orElse(null)).isSameAs(User);
        Mockito.verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("findByUsernameEmail() sukses get dao findByUsernameEmail")
    void findByUsernameEmail_shouldReturnUser_whenPresent() {
        String auth = "123123";
        User User = new User();

        Mockito.when(userRepository.findByUsername(auth)).thenReturn(Optional.empty());

        Mockito.when(userRepository.findByEmail(auth)).thenReturn(Optional.of(User));

        Optional<User> result = userDao.findByUsernameEmail(auth);

        assertThat(result.orElse(null)).isSameAs(User);
        Mockito.verify(userRepository).findByEmail(auth);
        Mockito.verify(userRepository).findByEmail(auth);
    }
}
