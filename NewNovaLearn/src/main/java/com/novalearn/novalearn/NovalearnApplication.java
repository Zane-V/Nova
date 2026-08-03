package com.novalearn.novalearn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class NovalearnApplication {

    public static void main(String[] args) {
        SpringApplication.run(NovalearnApplication.class, args);
    }

}
