package com.library.libraryService.config;

import com.library.libraryService.user.dao.UserDao;
import com.library.libraryService.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${app.init-super-admin:true}")
    private boolean initSuperAdmin;

    @Override
    public void run(String... args) {
        if (!initSuperAdmin) {
            return;
        }

        long count = userDao.count();
        if (count > 0) {
            return;
        }

        User admin = new User();
        admin.setUsername("fida");
        admin.setEmail("contact.fidamuhrifqi@gmail.com");
        admin.setPassword(passwordEncoder.encode("P@ssw0rd"));
        admin.setFullName("fida Super Admin");
        admin.setRole("SUPER_ADMIN");
        admin.setCreatedAt(new Date());
        admin.setUpdatedAt(new Date());

        userDao.save(admin);

        System.out.println("**** Default SUPER_ADMIN created (username: fida || password: P@ssw0rd)");
    }
}
