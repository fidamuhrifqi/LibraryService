package com.library.libraryService.user.dao;

import com.library.libraryService.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {

    User save(User user);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameEmail(String authName);
    List<User> getAllUser();
    void delete(User user);
    User findById(String id);
    long count();
}
