package com.litmind;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LitMindApplication {
    public static void main(String[] args) {
        SpringApplication.run(LitMindApplication.class, args);
    }
}

