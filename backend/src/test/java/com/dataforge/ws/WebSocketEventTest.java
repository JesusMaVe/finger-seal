package com.dataforge.ws;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketEventTest {

    @LocalServerPort
    private int port;

    @Autowired
    private EventPublisher eventPublisher;

    @Test
    void queryExecutedEventBroadcasted() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> received = new AtomicReference<>();

        StandardWebSocketClient client = new StandardWebSocketClient();
        WebSocketSession session = client.execute(new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession sess, TextMessage msg) {
                received.set(msg.getPayload());
                latch.countDown();
            }
        }, "ws://localhost:" + port + "/ws/events").get(5, TimeUnit.SECONDS);

        // Publish event
        eventPublisher.queryExecuted(1L, "SELECT 1", "SUCCESS", 5L, 1, null);

        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
        assertThat(received.get()).contains("SELECT 1");
        assertThat(received.get()).contains("SUCCESS");
        assertThat(received.get()).contains("\"type\":\"query\"");

        session.close();
    }
}
