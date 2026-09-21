package org.litebridge.db.spi.impl;

import org.litebridge.db.spi.util.SqlReservedWords;

public class LabelProcessor {

    public String createAliasAs(final String alias) {
        return " AS " + quoteAlias(alias);
    }

    public String quoteAlias(final String alias) {
        return "\"" + alias + "\"";
    }

    /**
     * Quotes a SQL identifier if it is a reserved word.
     *
     * @param identifier the identifier to potentially quote
     * @return the quoted (if necessary) or original identifier
     */
    public static String quoteIdentifier(final String identifier) {
        if (SqlReservedWords.contains(identifier)) {
            return "\"%s\"".formatted(identifier);
        } else {
            return identifier;
        }
    }
}
