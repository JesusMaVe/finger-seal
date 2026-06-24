package com.dataforge.editor;

import com.dataforge.editor.SqlLintResult.Issue;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.update.Update;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class SqlLintService {

    private static final Pattern MISSING_WHERE_RE = Pattern.compile(
        "(?i)\\b(UPDATE|DELETE)\\b"
    );
    private static final Pattern IMPLICIT_JOIN_RE = Pattern.compile(
        "(?i)\\bFROM\\s+\\w+\\s*,\\s*\\w+"
    );
    private static final Pattern LIKE_LEADING_WILDCARD = Pattern.compile(
        "(?i)LIKE\\s+'%"
    );

    public SqlLintResult lint(String sql) {
        long start = System.currentTimeMillis();
        List<Issue> issues = new ArrayList<>();

        if (sql == null || sql.trim().isEmpty()) {
            return new SqlLintResult(issues, 0);
        }

        // Regex-based checks (fast, no parse needed)
        addRegexIssues(sql, issues);

        // AST-based checks (requires successful parse)
        try {
            Statement stmt = CCJSqlParserUtil.parse(sql);
            addAstIssues(stmt, issues);
        } catch (JSQLParserException e) {
            String msg = e.getMessage();
            int line = 1;
            int col = 1;
            if (msg != null) {
                var lineMatch = Pattern.compile("line (\\d+)").matcher(msg);
                var colMatch = Pattern.compile("column (\\d+)").matcher(msg);
                if (lineMatch.find()) line = Integer.parseInt(lineMatch.group(1));
                if (colMatch.find()) col = Integer.parseInt(colMatch.group(1));
            }
            issues.add(new Issue(line, col, "Syntax error: " + cleanMessage(msg), "error"));
        }

        long elapsed = System.currentTimeMillis() - start;
        return new SqlLintResult(issues, elapsed);
    }

    private void addRegexIssues(String sql, List<Issue> issues) {
        // UPDATE/DELETE without WHERE
        String singleLine = sql.replaceAll("(?s)\\s+", " ");
        if (singleLine.matches("(?i).*(UPDATE|DELETE).*") && !singleLine.matches("(?i).*(UPDATE|DELETE).*WHERE.*")) {
            issues.add(new Issue(1, 1, "UPDATE/DELETE without WHERE clause — this will affect all rows", "warning"));
        }

        // Implicit comma joins
        if (IMPLICIT_JOIN_RE.matcher(sql).find()) {
            issues.add(new Issue(1, 1, "Implicit join detected — prefer explicit JOIN ... ON syntax", "info"));
        }

        // LIKE with leading wildcard
        if (LIKE_LEADING_WILDCARD.matcher(sql).find()) {
            issues.add(new Issue(1, 1, "LIKE with leading '%' prevents index usage", "info"));
        }
    }

    private void addAstIssues(Statement stmt, List<Issue> issues) {
        if (stmt instanceof Select select) {
            checkSelect(select, issues);
        } else if (stmt instanceof Update update) {
            checkUpdate(update, issues);
        }
    }

    private void checkSelect(Select select, List<Issue> issues) {
        if (select.getSelectBody() instanceof PlainSelect plain) {
            var cols = plain.getSelectItems();
            if (cols != null && cols.size() == 1) {
                // Check for SELECT * — the item will have expression "*"
                String itemStr = cols.get(0).toString().trim();
                if (itemStr.equals("*")) {
                    issues.add(new Issue(1, 8,
                        "SELECT * returns all columns — specify only the columns you need",
                        "warning"
                    ));
                }
            }

            if (plain.getWhere() == null && plain.getFromItem() != null) {
                issues.add(new Issue(1, 1, "SELECT without WHERE — will scan entire table", "info"));
            }
        }
    }

    private void checkUpdate(Update update, List<Issue> issues) {
        if (update.getWhere() == null) {
            issues.add(new Issue(1, 1,
                "UPDATE without WHERE clause — this will affect all rows",
                "warning"
            ));
        }
    }

    private String cleanMessage(String msg) {
        if (msg == null) return "Unknown error";
        return msg.length() > 200 ? msg.substring(0, 200) + "..." : msg;
    }
}
