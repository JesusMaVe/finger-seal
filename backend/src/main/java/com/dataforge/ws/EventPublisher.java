package com.dataforge.ws;

import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ponytail: simple fire-and-forget events. Replace with a proper event bus if fan-out grows.
 */
@Service
public class EventPublisher {

    private final QueryEventWebSocketHandler handler;

    public EventPublisher(QueryEventWebSocketHandler handler) {
        this.handler = handler;
    }

    public void queryExecuted(Long connectionId, String sql, String status,
                               long elapsedMs, Integer rows, String error) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("type", "query");
        event.put("connectionId", connectionId);
        event.put("sql", sql);
        event.put("status", status);
        event.put("elapsedMs", elapsedMs);
        event.put("rows", rows);
        event.put("error", error);
        event.put("timestamp", System.currentTimeMillis());
        handler.broadcast(event);
    }
}
