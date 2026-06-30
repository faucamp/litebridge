package org.litebridgedb.db.spi.impl;

import org.litebridgedb.commons.StringUtils;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Operation;
import org.litebridgedb.db.spi.util.SqlReservedWords;

/**
 * The ColumnIdentifierGenerator class provides utilities for generating SQL column identifiers
 * with optional table qualifiers, handling reserved keywords, and creating aliases for columns.
 * This class can be extended to implement database-specific customizations of column identifier generation.
 */
public class ColumnIdentifierGenerator {

    @SuppressWarnings("ConstantConditions")
    public String createSelectColumnIdentifier(final Column column, boolean includeColumnAlias, final Operation operation) {
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

    @SuppressWarnings("ConstantConditions")
    public String createColumnReference(final Column column) {
        return column.alias() != null ? quoteIdentifier(column.alias()) : quoteIdentifier(column.name());
    }

    public String quoteIdentifier(final String identifier) {
        if (SqlReservedWords.contains(identifier)) {
            return "\"%s\"".formatted(identifier);
        } else {
            return identifier;
        }
    }

    public String createAlias(final String alias) {
        return "AS %s".formatted(quoteIdentifier(alias));
    }
}
