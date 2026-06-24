package com.dataforge.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.Span;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class QueryEventWebSocketHandler extends TextWebSocketHandler {

    private final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private final Tracer tracer;

    public QueryEventWebSocketHandler(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    public void broadcast(Map<String, Object> event) {
        Span span = tracer.nextSpan().name("ws.broadcast").start();
        try (var ws = tracer.withSpan(span)) {
            span.tag("event.type", (String) event.getOrDefault("type", "unknown"));
            String json = mapper.writeValueAsString(event);
            TextMessage msg = new TextMessage(json);
            for (WebSocketSession s : sessions) {
                if (s.isOpen()) {
                    try { s.sendMessage(msg); } catch (IOException ignored) {}
                }
            }
        } catch (Exception e) {
            span.error(e);
        } finally {
            span.end();
        }
    }
}
