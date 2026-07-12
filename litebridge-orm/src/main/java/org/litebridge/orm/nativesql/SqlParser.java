package org.litebridge.orm.nativesql;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class for parsing SQL statements containing named parameters and converting them
 * into SQL statements with positional placeholders.
 * <p>
 * This class uses regular expressions to identify named parameters in the SQL statement,
 * replaces them with positional placeholders (e.g., "?"), and extracts a list of the original
 * parameter names in the order they appear in the statement. The output is encapsulated
 * in a {@code ParsedSql} object.
 * <p>
 * This is a final class with only static methods and is not intended to be instantiated.
 */
public final class SqlParser {

    private static final Pattern tokenPattern = Pattern.compile("('([^'\\\\]|\\\\.)*')|(\"([^\"\\\\]|\\\\.)*\")|(::)|(:([a-zA-Z0-9_]+))");
    private static final int GROUP_VARIABLE = 7;

    private SqlParser() {
    }

    public static ParsedSql parseSql(final String sql) {
        final StringBuilder outputSql = new StringBuilder();
        final List<String> extractedVariables = new ArrayList<>();

        final Matcher matcher = tokenPattern.matcher(sql);
        while (matcher.find()) {
            final String varName = matcher.group(GROUP_VARIABLE);

            if (varName != null) {
                extractedVariables.add(varName);
                // Replace the named parameter with a positional one
                matcher.appendReplacement(outputSql, "?");
            } else {
                // Not a named parameter; append the original token
                matcher.appendReplacement(outputSql, matcher.group());
            }
        }
        matcher.appendTail(outputSql);

        return new ParsedSql(outputSql.toString(), extractedVariables);
    }
}
