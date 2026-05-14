package com.wealthsense.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringAiConfig {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder
            .defaultSystem("""
                You are WealthSense AI, a personal financial advisor for Indians.
                You help users understand their spending, save money, and make smart financial decisions.
                
                Rules:
                - Only answer finance-related questions
                - Never give specific stock tips
                - Always be encouraging and helpful
                - Use simple language (not jargon)
                - Reference user's actual data when available
                - Give advice in Indian context (INR, Indian banks)
                - Keep responses under 200 words
                """)
            .build();
    }
}
