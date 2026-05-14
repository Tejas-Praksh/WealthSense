package com.wealthsense.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.wealthsense")
public class AiAdvisorApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiAdvisorApplication.class, args);
    }
}
