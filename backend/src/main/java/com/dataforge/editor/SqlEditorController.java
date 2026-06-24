package com.dataforge.editor;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/editor")
public class SqlEditorController {

    private final SqlLintService lintService;
    private final SqlFormatService formatService;
    private final AutoCompleteService autoCompleteService;

    public SqlEditorController(SqlLintService lintService, SqlFormatService formatService, AutoCompleteService autoCompleteService) {
        this.lintService = lintService;
        this.formatService = formatService;
        this.autoCompleteService = autoCompleteService;
    }

    public record LintRequest(String sql, String dialect) {}
    public record FormatRequest(String sql) {}
    public record SuggestRequest(Long connectionId, String partial) {}

    @PostMapping("/lint")
    public SqlLintResult lint(@RequestBody LintRequest request) {
        return lintService.lint(request.sql());
    }

    @PostMapping("/format")
    public SqlFormatService.FormatResult format(@RequestBody FormatRequest request) {
        return formatService.format(request.sql());
    }

    @PostMapping("/suggest")
    public AutoCompleteService.AutoCompleteResult suggest(@RequestBody SuggestRequest request) {
        return autoCompleteService.getSuggestions(request.connectionId(), request.partial());
    }
}
