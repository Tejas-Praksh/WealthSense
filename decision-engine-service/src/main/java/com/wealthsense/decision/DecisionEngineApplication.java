package com.wealthsense.decision;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.wealthsense")
public class DecisionEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(DecisionEngineApplication.class, args);
    }
}
