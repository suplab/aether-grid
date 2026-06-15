package com.suplab.aether.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.suplab.aether")
@EnableScheduling
public class AetherApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AetherApiApplication.class, args);
    }
}
