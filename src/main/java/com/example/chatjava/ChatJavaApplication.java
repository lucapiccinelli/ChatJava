package com.example.chatjava;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@SpringBootApplication
public class ChatJavaApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatJavaApplication.class, args);
    }
}


