package com.example.chatjava;

import com.example.chatjava.model.ChatMessage;
import com.example.chatjava.model.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {
    private final Logger log = LoggerFactory.getLogger(ChatController.class);

    @SubscribeMapping("/public")
    public ChatMessage welcome(){
        return new ChatMessage("Chat Bot", "welcome to topic/public", MessageType.CHAT);
    }

    @MessageMapping("/chat.register")
    @SendTo("/topic/public")
    public ChatMessage register(@Payload ChatMessage message, SimpMessageHeaderAccessor headerAccessor){
        headerAccessor.getSessionAttributes().put("username", message.getSender());
        return message;
    }

    @MessageMapping("/chat.send")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage message, SimpMessageHeaderAccessor headerAccessor){
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        log.info(String.format("message from %s", username));
        return message;
    }

    @MessageMapping("/chat.send.{destinationUser}")
    @SendTo("/topic/{destinationUser}")
    @SendToUser("/topic/me")
    ChatMessage sendMessageToUser(
            @Payload ChatMessage message,
            @DestinationVariable String destinationUser,
            SimpMessageHeaderAccessor headerAccessor){
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        log.info(String.format("message from %s to %s", username, destinationUser));
        return message;
    }
}

