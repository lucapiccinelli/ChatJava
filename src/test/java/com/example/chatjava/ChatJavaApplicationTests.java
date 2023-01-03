package com.example.chatjava;

import com.example.chatjava.model.ChatMessage;
import com.example.chatjava.model.MessageType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.*;

import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {ChatJavaApplication.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ChatJavaApplicationTests {

    @LocalServerPort
    public Integer port;

    WebSocketStompClient websocket;

    @BeforeAll
    public void setupAll(){
        websocket =
            new WebSocketStompClient(
                new SockJsClient(
                        List.of(new WebSocketTransport(new StandardWebSocketClient()))));
        websocket.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    void whenSomeoneRegister_everyoneReceivesAJoinNotification() throws Exception {
        StompSession session = websocket
            .connectAsync(String.format("ws://localhost:%d/chat", port), new StompSessionHandlerAdapter() {})
            .get(1, TimeUnit.SECONDS);

        BlockingQueue<ChatMessage> receivedMessages = new ArrayBlockingQueue<>(2);
        session.subscribe("/topic/public", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                receivedMessages.add((ChatMessage) payload);
            }
        });

        ChatMessage sentMessage = new ChatMessage("banana", null, MessageType.JOIN);
        session.send("/app/chat.register", sentMessage);

        await()
                .atMost(1, TimeUnit.SECONDS)
                .untilAsserted(() -> Assertions.assertEquals(sentMessage, receivedMessages.poll()));
    }

}
