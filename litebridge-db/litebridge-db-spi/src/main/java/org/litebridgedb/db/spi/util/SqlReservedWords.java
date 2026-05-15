package org.litebridgedb.db.spi.util;

import java.util.Set;

public final class SqlReservedWords {

    private SqlReservedWords() {
    }

    private static final Set<String> reservedWords = Set.of(
            "SELECT",
            "INSERT",
            "UPDATE",
            "DELETE",
            "FROM",
            "WHERE",
            "AND",
            "OR",
            "NOT",
            "CREATE",
            "ALTER",
            "DROP",
            "TABLE",
            "COLUMN",
            "INDEX",
            "JOIN",
            "ON",
            "GROUP",
            "BY",
            "HAVING",
            "ORDER",
            "UNION",
            "ALL",
            "AS",
            "CASE",
            "WHEN",
            "THEN",
            "ELSE",
            "END",
            "NULL",
            "TRUE",
            "FALSE",
            "ASC",
            "DESC"
    );

    public static boolean contains(final String word) {
        return reservedWords.contains(word.toUpperCase());
    }
}
