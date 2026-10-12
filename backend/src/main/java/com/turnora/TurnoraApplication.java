package com.turnora;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TurnoraApplication {

    public static void main(String[] args) {
        SpringApplication.run(TurnoraApplication.class, args);
    }
}