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

    /**
     * Creates a SQL identifier for a column to be used in a SELECT clause.
     *
     * @param column    the column for which to create the identifier
     * @param operation the current database operation
     * @param clause    the SQL clause where the identifier will be used
     * @param nested    whether the column is part of a nested expression
     * @return the generated SQL column identifier
     */
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

    /**
     * Creates a SQL reference to a column.
     *
     * @param column    the column to reference
     * @param operation the current database operation
     * @param clause    the SQL clause where the reference will be used
     * @return the generated SQL column reference
     */
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

    /**
     * Quotes a SQL identifier if it is a reserved word.
     *
     * @param identifier the identifier to potentially quote
     * @return the quoted (if necessary) or original identifier
     */
    public String quoteIdentifier(final String identifier) {
        if (SqlReservedWords.contains(identifier)) {
            return "\"%s\"".formatted(identifier);
        } else {
            return identifier;
        }
    }

    /**
     * Creates a SQL alias declaration.
     *
     * @param alias the alias to declare
     * @return the SQL alias declaration (e.g., "AS alias")
     */
    public String createAliasDeclaration(final String alias) {
        return "AS %s".formatted(quoteIdentifier(alias));
    }
}
