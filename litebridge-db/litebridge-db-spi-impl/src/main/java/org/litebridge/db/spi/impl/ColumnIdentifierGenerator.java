package org.litebridge.db.spi.impl;

import org.litebridge.commons.StringUtils;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.util.SqlReservedWords;

/**
 * The ColumnIdentifierGenerator class provides utilities for generating SQL column identifiers
 * with optional table qualifiers, handling reserved keywords, and creating aliases for columns.
 * This class can be extended to implement database-specific customizations of column identifier generation.
 */
public class ColumnIdentifierGenerator {

    public String createSelectColumn(final Column column, final Operation operation, final ClauseType clause, final boolean nested) {
        final StringBuilder sb = new StringBuilder();
        final Table table = column.table();

        if (!StringUtils.isBlank(table.alias())) {
            //noinspection DataFlowIssue
            sb.append(quoteIdentifier(table.alias()));
        } else {
            sb.append(quoteIdentifier(table.name()));
        }

        sb.append('.').append(quoteIdentifier(column.name()));

        if (!nested && column.alias() != null) {
            //noinspection DataFlowIssue
            sb.append(' ').append(createAliasDeclaration(column.alias()));
        }

        return sb.toString();
    }

    public String createColumnRef(final Column column, final Operation operation, final ClauseType clause) {
        if (column.alias() != null && clause != ClauseType.WHERE) {
            //noinspection DataFlowIssue
            return column.alias();
        }

        if (column.table().alias() != null) {
            return column.table().alias() + "." + column.name();
        }

        return column.table().name() + "." + column.name();
    }

    public String quoteIdentifier(final String identifier) {
        if (SqlReservedWords.contains(identifier)) {
            return "\"%s\"".formatted(identifier);
        } else {
            return identifier;
        }
    }

    public String createAliasDeclaration(final String alias) {
        return "AS %s".formatted(quoteIdentifier(alias));
    }
}
