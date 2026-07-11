package org.litebridgedb.db.spi.impl;

import org.litebridgedb.commons.StringUtils;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Operation;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.expression.ClauseType;
import org.litebridgedb.db.spi.util.SqlReservedWords;

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
