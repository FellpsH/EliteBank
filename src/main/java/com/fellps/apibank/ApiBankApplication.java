package com.fellps.apibank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ApiBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiBankApplication.class, args);
    }

}

