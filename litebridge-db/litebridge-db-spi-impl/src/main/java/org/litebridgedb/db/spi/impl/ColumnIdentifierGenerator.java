package org.litebridgedb.db.spi.impl;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.StringUtils;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.util.SqlReservedWords;

public class ColumnIdentifierGenerator {

    public String createColumnIdentifier(final Column column, boolean includeColumnAlias) {
        final StringBuilder columnSql = new StringBuilder();

        if (!StringUtils.isEmpty(column.table().alias())) {
            columnSql.append(quoteIdentifier(column.table().alias()));
        } else {
            columnSql.append(quoteIdentifier(column.table().name()));
        }

        columnSql.append('.').append(quoteIdentifier(column.name()));

        if (includeColumnAlias && !StringUtils.isBlank(column.alias())) {
            columnSql.append(' ').append(createAlias(quoteIdentifier(column.alias())));
        }

        return columnSql.toString();
    }

    protected @Nullable String quoteIdentifier(final @Nullable String identifier) {
        if (identifier == null) {
            return null;
        }

        if (SqlReservedWords.contains(identifier)) {
            return "\"%s\"".formatted(identifier);
        } else {
            return identifier;
        }
    }

    protected String createAlias(final String alias) {
        return "AS %s".formatted(alias);
    }
}
