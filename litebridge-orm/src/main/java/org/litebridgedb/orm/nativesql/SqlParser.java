package org.litebridgedb.orm.nativesql;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SqlParser {

    private static final Pattern tokenPattern = Pattern.compile("('([^'\\\\]|\\\\.)*')|(\"([^\"\\\\]|\\\\.)*\")|(::)|(:([a-zA-Z0-9_]+))");
    private static final int GROUP_VARIABLE = 7;

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

        return new ParsedSql(outputSql.toString(), extractedVariables);
    }
}
