package com.example.chatjava;

import com.example.chatjava.model.ChatMessage;
import com.example.chatjava.model.MessageType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.socket.WebSocketHttpHeaders;
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

    @Autowired
    public TestRestTemplate rest;

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
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJDdXN0b21JbmZvIjp7ImxvY2FsaXphdGlvbkNvZGUiOiJJVEEiLCJ1c2VybmFtZSI6ImluZm9sYWIiLCJpMThuQ29kZSI6Iml0LUlUIiwiZmlyc3ROYW1lIjoiaW5mb2xhYiIsImxhc3ROYW1lIjoidGVzdCIsInJlZ2lvbkNvZGUiOiIwMyIsInRlbmFudElkIjoxMDk0LCJwYXJlbnRUZW5hbnRJZCI6MTA5NCwicm9vdFRlbmFudElkIjoxMDk0LCJncm91cExldmVsIjoiU1AiLCJ1c2VySWQiOjIzMjAsInRpbWV6b25lTmFtZSI6IkV1cm9wZS9Sb21lIiwiZW5jb2RlZEF1dGh6IjoiLy8vLy8vLy8vdz09IiwibG9nTGV2ZWwiOiJJTkZPIiwibXVzdENoYW5nZVRoZVBhc3N3b3JkIjp0cnVlLCJzdG9ja01vZHVsZUVuYWJsZWQiOnRydWUsImlkIjoyMzIwLCJpZGVudGlmaWVyIjoiaW5mb2xhYiIsIndvcmtzdGF0aW9uIjp7InVuaXF1ZUlkZW50aWZpZXIiOiIiLCJ3b3Jrc3RhdGlvbkZpc2NhbElkIjoiIn0sImRlbnlSZWxlYXNlRkYiOlsiU0ZEQiJdLCJhbGxvd0xpY2VuY2VGRiI6bnVsbH0sImV4cCI6MTY3MzUyMDQxNiwidXNlcl9uYW1lIjoiaW5mb2xhYiIsImp0aSI6IjA0ZjMxMWE3LWMxNmQtNGNkOS1hNzliLTlkZDJkMjA0ZmU3MyIsImNsaWVudF9pZCI6ImluZm9sYWIiLCJzY29wZSI6WyJpbmZvbGFiIl19.eMH-nY40xwNbLRrW4CPlDX_VJkyktJIMhl2AaQiyeHjQiK6tzvRkEPoPZAvP_mkd7qeTaePkx3CnFWUfAdO8vJrulrf5pPZKDRt6m0ttR1dlKNXdk8I2lT2MfPbMDdLjq2vkDi7ucKVGf4W284UNeQ7EM9frVPn7EJ4sKxDH2wG4Xyvcy8SoQVpK35WMxQ-JBe79g8h5VK-o1po-MnFltlu6nz1U0JOhBjaY-uHKNbPEiiBBfzCd8cUrRCoV83FFAl9mmuudG2PWC4Oq9wxsAJBp8kgT-rtrlBkRB48RXf06MDs4-Pz0_yQP9g1--Mjtd3lOcWvf1K-z0Qwc93Fb8g";
        ResponseEntity<MyCsrfToken> csrfResponse = rest.exchange(
                RequestEntity
                    .get("/csrf")
                    .build(),
                MyCsrfToken.class);

        StompHeaders stompHeaders = new StompHeaders();
        MyCsrfToken csrf = csrfResponse.getBody();
        stompHeaders.add(csrf.headerName(), csrf.token());

        StompSession session = websocket
            .connectAsync(String.format("http://localhost:%d/chat?access_token=%s", port, token), new WebSocketHttpHeaders(), stompHeaders, new StompSessionHandlerAdapter() {})
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
record MyCsrfToken(String headerName, String parameterName, String token){}