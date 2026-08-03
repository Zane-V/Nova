package com.novalearn.novalearn.config;

import com.novalearn.novalearn.repository.CourseRepository;
import com.novalearn.novalearn.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initData(UserRepository userRepository, CourseRepository courseRepository) {
        return args -> {
            log.info("Ready for custom user registrations!");
        };
    }
}
