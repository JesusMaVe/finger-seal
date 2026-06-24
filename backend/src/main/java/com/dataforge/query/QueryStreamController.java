package com.dataforge.query;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * SSE streaming endpoint for query results.
 *
 * Usage: curl -N -X POST "http://localhost:8080/api/query/stream" \
 *   -H "Content-type: application/json" \
 *   -d '{"connectionId":1,"sql":"SELECT * FROM large_table"}'
 *
 * Returns: SSE stream with header/row/complete/error events.
 * Uses StreamingResponseBody (Servlet 3.1 async) — no WebFlux dependency needed.
 *
 * ponytail: 5min timeout for long-running queries.
 */
@RestController
@RequestMapping("/api/query")
public class QueryStreamController {

    private final QueryStreamService queryStreamService;

    public QueryStreamController(QueryStreamService queryStreamService) {
        this.queryStreamService = queryStreamService;
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public StreamingResponseBody streamQuery(@RequestBody Map<String, Object> request) {
        Long connectionId = Long.valueOf(request.get("connectionId").toString());
        String sql = (String) request.get("sql");

        return (OutputStream outputStream) -> {
            var writer = new java.io.PrintWriter(outputStream, true, StandardCharsets.UTF_8);

            queryStreamService.streamQuery(
                connectionId,
                sql,
                row -> {
                    writer.print("data: " + row + "\n\n");
                    writer.flush();
                },
                writer::close,
                error -> {
                    writer.print("data: " + error + "\n\n");
                    writer.flush();
                    writer.close();
                }
            );
        };
    }
}
