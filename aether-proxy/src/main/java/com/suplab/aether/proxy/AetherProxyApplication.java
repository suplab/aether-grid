package com.suplab.aether.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AetherProxyApplication {

    public static void main(String[] args) {
        SpringApplication.run(AetherProxyApplication.class, args);
    }
}
