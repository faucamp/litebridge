package org.litebridgedb.db.oracle;

import org.litebridgedb.commons.StringUtils;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Operation;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.query.Condition;
import org.litebridgedb.db.spi.query.Join;
import org.litebridgedb.db.spi.query.Operator;
import org.litebridgedb.db.spi.query.Select;

public final class OracleColumnIdentifierGenerator extends ColumnIdentifierGenerator {

    @Override
    public String createSelectColumnIdentifier(final Column column, final boolean includeColumnAlias, final Operation operation) {
        if (!(operation instanceof final Select select)) {
            return super.createSelectColumnIdentifier(column, includeColumnAlias, operation);
        }

        boolean applyTableQualifier = true;

        // If a JOIN USING is used in the select from/where/using clause, Oracle doesn't allow table qualifiers for the column
        for (Join join : select.joins()) {
            for (Condition condition : join.conditions()) {
                if (condition.operator() == Operator.USING
                        // Same column
                        && (condition.column().equalsIgnoreAlias(column)
                        // Same column but from other side of join
                        || (condition.column().equalsColumnOnlyIgnoreAlias(column)
                        && (select.table().equalsIgnoreAlias(column.table()) || join.table().equalsIgnoreAlias(column.table()))))) {
                    // Don't include table qualifiers
                    applyTableQualifier = false;
                    break;
                }
            }

            if (!applyTableQualifier) {
                break;
            }
        }

        if (applyTableQualifier) {
            return super.createSelectColumnIdentifier(column, includeColumnAlias, select);
        }

        final StringBuilder columnSql = new StringBuilder(quoteIdentifier(column.name()));

        if (includeColumnAlias && !StringUtils.isBlank(column.alias())) {
            columnSql.append(' ').append(createAlias(quoteIdentifier(column.alias())));
        }

        return columnSql.toString();
    }

    @Override
    public String createAlias(final String alias) {
        return quoteIdentifier(alias);
    }
}
