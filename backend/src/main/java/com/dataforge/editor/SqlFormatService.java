package com.dataforge.editor;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.springframework.stereotype.Service;

@Service
public class SqlFormatService {

    public record FormatResult(String sql, String error, long elapsedMs) {}

    public FormatResult format(String sql) {
        long start = System.currentTimeMillis();
        if (sql == null || sql.trim().isEmpty()) {
            return new FormatResult("", null, 0);
        }

        try {
            Statement stmt = CCJSqlParserUtil.parse(sql);
            // JSqlParser toString() outputs normalized SQL from the AST
            String formatted = stmt.toString().trim();
            // Normalize multiple spaces to single space
            formatted = formatted.replaceAll("[ \\t]+", " ");
            long elapsed = System.currentTimeMillis() - start;
            return new FormatResult(formatted, null, elapsed);
        } catch (JSQLParserException e) {
            long elapsed = System.currentTimeMillis() - start;
            return new FormatResult(null, "Cannot format: " + cleanMessage(e.getMessage()), elapsed);
        }
    }

    private String cleanMessage(String msg) {
        if (msg == null) return "Parse error";
        return msg.length() > 200 ? msg.substring(0, 200) + "..." : msg;
    }
}
