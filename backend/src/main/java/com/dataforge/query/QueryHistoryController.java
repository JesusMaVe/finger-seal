package com.dataforge.query;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/connections/{connectionId}/history")
public class QueryHistoryController {

    private final QueryHistoryRepository repo;

    public QueryHistoryController(QueryHistoryRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<QueryHistory> list(@PathVariable Long connectionId) {
        return repo.findByConnectionIdOrderByCreatedAtDesc(connectionId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(@PathVariable Long connectionId) {
        repo.deleteByConnectionId(connectionId);
    }
}
