package com.dataforge.editor;

import java.util.List;

public record SqlLintResult(
    List<Issue> issues,
    long elapsedMs
) {
    public record Issue(
        int line,
        int column,
        String message,
        String severity  // "error" | "warning" | "info"
    ) {}
}
