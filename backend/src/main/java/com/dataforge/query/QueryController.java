package com.dataforge.query;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/query")
public class QueryController {

    private final QueryService queryService;

    public QueryController(QueryService queryService) {
        this.queryService = queryService;
    }

    @PostMapping
    public QueryResult execute(@Valid @RequestBody QueryRequest request) {
        return queryService.execute(request);
    }

    @PostMapping("/inline-edit")
    public QueryResult inlineEdit(@Valid @RequestBody InlineEditRequest request) {
        return queryService.inlineEdit(request);
    }
}
