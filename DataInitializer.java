package com.medicinereminder.config;

import com.medicinereminder.entity.User;
import com.medicinereminder.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    public static final String DEV_LINE_USER_ID = "dev-user";

    @Bean
    CommandLineRunner seedDefaultUser(UserRepository userRepository) {
        return args -> {
            if (userRepository.findByLineUserId(DEV_LINE_USER_ID).isEmpty()) {
                User user = new User();
                user.setLineUserId(DEV_LINE_USER_ID);
                user.setDisplayName("開発ユーザー");
                userRepository.save(user);
            }
        };
    }
}
